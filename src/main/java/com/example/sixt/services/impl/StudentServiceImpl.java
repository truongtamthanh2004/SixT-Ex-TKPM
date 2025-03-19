package com.example.sixt.services.impl;

import com.example.sixt.controllers.requests.AddressRequest;
import com.example.sixt.controllers.requests.StudentCreationRequest;
import com.example.sixt.controllers.requests.StudentUpdateRequest;
import com.example.sixt.controllers.responses.StudentResponse;
import com.example.sixt.exceptions.InvalidDataException;
import com.example.sixt.models.AddressEntity;
import com.example.sixt.models.IdentityDocumentEntity;
import com.example.sixt.models.StudentEntity;
import com.example.sixt.repositories.*;
import com.example.sixt.services.StudentService;
import org.modelmapper.ModelMapper;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class StudentServiceImpl implements StudentService {
    private final StudentRepository studentRepository;
    private final ModelMapper modelMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedissonClient redissonClient;
    private final AddressRepository addressRepository;
    private final IdentityDocumentRepository identityDocumentRepository;
    private final ProgramRepository programRepository;
    private final DepartmentRepository departmentRepository;
    private final StudentStatusRepository studentStatusRepository;

    @Autowired
    public StudentServiceImpl(StudentRepository studentRepository,
                              ModelMapper modelMapper,
                              RedisTemplate<String, Object> redisTemplate,
                              RedissonClient redissonClient,
                              AddressRepository addressRepository,
                              IdentityDocumentRepository identityDocumentRepository,
                              ProgramRepository programRepository,
                              DepartmentRepository departmentRepository,
                              StudentStatusRepository studentStatusRepository) {
        this.studentRepository = studentRepository;
        this.modelMapper = modelMapper;
        this.redisTemplate = redisTemplate;
        this.redissonClient = redissonClient;
        this.addressRepository = addressRepository;
        this.identityDocumentRepository = identityDocumentRepository;
        this.programRepository = programRepository;
        this.departmentRepository = departmentRepository;
        this.studentStatusRepository = studentStatusRepository;
    }

    @Override
    @Transactional
    @CacheEvict(value = "students", key = "#student.studentId")
    public StudentResponse addStudent(StudentCreationRequest student) {
        RLock lock = redissonClient.getReadWriteLock("lock:student:" + student.getStudentId()).writeLock();
        boolean isLocked = false;

        try {
            isLocked = lock.tryLock(10, 50, TimeUnit.SECONDS);
            if (!isLocked) {
                throw new RuntimeException("Error getting lock");
            }
            if (studentRepository.findByStudentId(student.getStudentId()) != null) {
                throw new InvalidDataException("Student id already exists.");
            }

            if (studentRepository.findByEmail(student.getEmail()) != null) {
                throw new InvalidDataException("Email already exists.");
            }

            if (student.getDepartment() != null && departmentRepository.findByName(student.getDepartment()) == null) {
                throw new InvalidDataException("Department does not exist.");
            }
            if (student.getProgram() != null && programRepository.findByName(student.getProgram()) == null) {
                throw new InvalidDataException("Program does not exist.");
            }
            if (student.getStatus() != null && studentStatusRepository.findByName(student.getStatus()) == null) {
                throw new InvalidDataException("Status does not exist.");
            }

            StudentEntity studentEntity = modelMapper.map(student, StudentEntity.class);
            studentEntity.setId(null);
            studentEntity.setNationality(student.getNationality());
            List<AddressEntity> addressEntities = student.getAddresses().stream()
                    .map(address -> modelMapper.map(address, AddressEntity.class))
                    .collect(Collectors.toList());
            addressEntities.forEach(address -> address.setStudentId(studentEntity.getStudentId()));
            IdentityDocumentEntity identityDocumentEntity = modelMapper.map(student.getIdentityDocument(), IdentityDocumentEntity.class);
            identityDocumentEntity.setStudentId(studentEntity.getStudentId());
            List<AddressEntity> savedAddressEntities = addressRepository.saveAll(addressEntities);
            IdentityDocumentEntity identityDocument = identityDocumentRepository.save(identityDocumentEntity);
            StudentEntity savedStudent = studentRepository.save(studentEntity);

            StudentResponse studentResponse = modelMapper.map(savedStudent, StudentResponse.class);
            studentResponse.setAddresses(savedAddressEntities);
            studentResponse.setIdentityDocument(identityDocument);

            redisTemplate.opsForValue().set("student:" + student.getStudentId(), studentResponse);
            return studentResponse;
        }
        catch (InvalidDataException e) {
            throw new InvalidDataException(e.getMessage());
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "students", key = "#studentId")
    public void deleteStudent(String studentId) {
        RLock lock = redissonClient.getReadWriteLock("lock:student:" + studentId).writeLock();

        try {
            if (!lock.tryLock(10, 100, TimeUnit.SECONDS)) {
                throw new RuntimeException("Cannot acquire lock for student " + studentId);
            }

            StudentEntity student = studentRepository.findByStudentId(studentId);
            if (student != null) {
                studentRepository.delete(student);
                addressRepository.deleteAllByStudentId(studentId);
                identityDocumentRepository.deleteByStudentId(studentId);

                redissonClient.getBucket("student:" + studentId).delete();
            } else {
                throw new InvalidDataException("Student not found.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread was interrupted while trying to acquire lock.");
        } catch (InvalidDataException e) {
            throw new InvalidDataException(e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Error deleting student.", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }


    @Override
    @Transactional
    @CacheEvict(value = "students", key = "#studentId")
    public StudentResponse updateStudent(String studentId, StudentUpdateRequest updatedStudent) {
        RLock lock = redissonClient.getReadWriteLock("lock:student:" + studentId).writeLock();

        try {
            if (!lock.tryLock(10, 100, TimeUnit.SECONDS)) {
                throw new RuntimeException("Cannot acquire lock for student " + studentId);
            }

            StudentEntity existingStudent = studentRepository.findByStudentId(studentId);
            if (existingStudent == null) {
                throw new InvalidDataException("Student not found.");
            }

            updateStudentFields(existingStudent, updatedStudent);
            List <AddressEntity> savedAddressEntities = new ArrayList<>();
            IdentityDocumentEntity savedIdentityDocument = new IdentityDocumentEntity();
            if (updatedStudent.getAddresses() != null) {
                addressRepository.deleteAllByStudentId(studentId);
                List<AddressEntity> addressEntities = updatedStudent.getAddresses().stream()
                        .map(address -> modelMapper.map(address, AddressEntity.class))
                        .collect(Collectors.toList());
                addressEntities.forEach(address -> address.setStudentId(studentId));
                savedAddressEntities = addressRepository.saveAll(addressEntities);
            }

            if (updatedStudent.getIdentityDocument() != null) {
                identityDocumentRepository.deleteByStudentId(studentId);
                IdentityDocumentEntity identityDocumentEntity = modelMapper.map(updatedStudent.getIdentityDocument(), IdentityDocumentEntity.class);
                identityDocumentEntity.setStudentId(studentId);
                savedIdentityDocument = identityDocumentRepository.save(identityDocumentEntity);
            }

            studentRepository.save(existingStudent);

            StudentResponse studentResponse = modelMapper.map(existingStudent, StudentResponse.class);
            studentResponse.setAddresses(savedAddressEntities.size() != 0 ? savedAddressEntities : addressRepository.findAllByStudentId(studentId));
            studentResponse.setIdentityDocument(savedIdentityDocument.getId() != null ? savedIdentityDocument : identityDocumentRepository.findByStudentId(studentId));

            redisTemplate.opsForValue().set("student:" + studentId, studentResponse);
            return studentResponse;
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread was interrupted while trying to acquire lock.");
        }
        catch (InvalidDataException e) {
            throw new InvalidDataException(e.getMessage());
        }
        catch (Exception e) {
            throw new RuntimeException("Error updating student.");
        }
        finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public List<StudentResponse> searchStudents(String keyword) {
        List<StudentResponse> results = new ArrayList<>();

        if (keyword.matches("\\d+")) {
            RLock lock = redissonClient.getReadWriteLock("lock:student:" + keyword).readLock();

            try {
                if (lock.tryLock(10, 100, TimeUnit.SECONDS)) {
                    StudentEntity cachedStudent = (StudentEntity) redisTemplate.opsForValue().get("student:" + keyword);
                    if (cachedStudent != null) {
                        StudentResponse studentResponse = modelMapper.map(cachedStudent, StudentResponse.class);
                        studentResponse.setAddresses(addressRepository.findAllByStudentId(cachedStudent.getStudentId()));
                        studentResponse.setIdentityDocument(identityDocumentRepository.findByStudentId(cachedStudent.getStudentId()));
                        results.add(studentResponse);
                        return results;
                    }

                    StudentEntity student = studentRepository.findByStudentId(keyword);
                    if (student != null) {
                        StudentResponse studentResponse = modelMapper.map(student, StudentResponse.class);
                        studentResponse.setAddresses(addressRepository.findAllByStudentId(student.getStudentId()));
                        studentResponse.setIdentityDocument(identityDocumentRepository.findByStudentId(student.getStudentId()));
                        redisTemplate.opsForValue().set("student:" + student.getStudentId(), studentResponse);
                        results.add(studentResponse);
                    }
                } else {
                    throw new RuntimeException("Cannot acquire lock for student search: " + keyword);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread interrupted while searching student.");
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
            return results;
        }

        RLock lock = redissonClient.getReadWriteLock("lock:student:search").readLock();
        try {
            if (lock.tryLock(0, 100, TimeUnit.SECONDS)) {
                Set<String> keys = redisTemplate.keys("student:*");
                if (keys != null) {
                    for (String key : keys) {
                        StudentEntity student = (StudentEntity) redisTemplate.opsForValue().get(key);
                        if (student != null && student.getFullName().toLowerCase().contains(keyword.toLowerCase())) {
                            StudentResponse studentResponse = modelMapper.map(student, StudentResponse.class);
                            studentResponse.setAddresses(addressRepository.findAllByStudentId(student.getStudentId()));
                            studentResponse.setIdentityDocument(identityDocumentRepository.findByStudentId(student.getStudentId()));
                            results.add(studentResponse);
                        }
                    }
                }

                if (results.isEmpty()) {
                    List<StudentEntity> studentEntities = studentRepository.findByStudentIdOrFullName(keyword);

                    for (StudentEntity student : studentEntities) {
                        StudentResponse studentResponse = modelMapper.map(student, StudentResponse.class);
                        studentResponse.setAddresses(addressRepository.findAllByStudentId(student.getStudentId()));
                        studentResponse.setIdentityDocument(identityDocumentRepository.findByStudentId(student.getStudentId()));
                        results.add(studentResponse);
                        redisTemplate.opsForValue().set("student:" + student.getStudentId(), studentResponse);
                    }
                }
            } else {
                throw new RuntimeException("Cannot acquire lock for student name search: " + keyword);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread interrupted while searching students by name.");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

        return results;
    }

    public void updateStudentFields(StudentEntity existingStudent, StudentUpdateRequest updatedStudent) {
        if (updatedStudent.getFullName() != null) {
            existingStudent.setFullName(updatedStudent.getFullName());
        }
        if (updatedStudent.getBirthday() != null) {
            existingStudent.setBirthday(updatedStudent.getBirthday());
        }
        if (updatedStudent.getGender() != null) {
            existingStudent.setGender(updatedStudent.getGender());
        }
        if (updatedStudent.getDepartment() != null && departmentRepository.findByName(updatedStudent.getDepartment()) != null) {
            existingStudent.setDepartment(updatedStudent.getDepartment());
        }
        if (updatedStudent.getCourse() != null) {
            existingStudent.setCourse(updatedStudent.getCourse());
        }
        if (updatedStudent.getProgram() != null && programRepository.findByName(updatedStudent.getProgram()) != null) {
            existingStudent.setProgram(updatedStudent.getProgram());
        }

        if (updatedStudent.getPhoneNumber() != null) {
            existingStudent.setPhoneNumber(updatedStudent.getPhoneNumber());
        }
        if (updatedStudent.getStatus() != null && studentStatusRepository.findByName(updatedStudent.getStatus()) != null) {
            existingStudent.setStatus(updatedStudent.getStatus());
        }
    }


}
