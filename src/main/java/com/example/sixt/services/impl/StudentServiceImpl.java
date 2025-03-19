package com.example.sixt.services.impl;

import com.example.sixt.controllers.requests.AddressRequest;
import com.example.sixt.controllers.requests.StudentCreationRequest;
import com.example.sixt.controllers.requests.StudentUpdateRequest;
import com.example.sixt.controllers.responses.StudentResponse;
import com.example.sixt.exceptions.InvalidDataException;
import com.example.sixt.models.*;
import com.example.sixt.repositories.*;
import com.example.sixt.services.StudentService;
import org.modelmapper.ModelMapper;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(StudentServiceImpl.class);

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

            DepartmentEntity departmentEntity = departmentRepository.findByName(student.getDepartment());
            if (student.getDepartment() != null && departmentEntity == null) {
                throw new InvalidDataException("Department does not exist.");
            }
            ProgramEntity programEntity = programRepository.findByName(student.getProgram());
            if (student.getProgram() != null && programEntity == null) {
                throw new InvalidDataException("Program does not exist.");
            }
            StudentStatusEntity studentStatusEntity = studentStatusRepository.findByName(student.getStatus());
            if (student.getStatus() != null && studentStatusEntity == null) {
                throw new InvalidDataException("Status does not exist.");
            }

            StudentEntity studentEntity = modelMapper.map(student, StudentEntity.class);
            studentEntity.setId(null);
            studentEntity.setDepartment(departmentEntity.getId());
            studentEntity.setProgram(programEntity.getId());
            studentEntity.setStatus(studentStatusEntity.getId());
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
            studentResponse.setDepartment(departmentEntity.getName());
            studentResponse.setProgram(programEntity.getName());
            studentResponse.setStatus(studentStatusEntity.getName());

            redisTemplate.opsForValue().set("student:" + student.getStudentId(), savedStudent);

            log.info("Student added successfully.");

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

                log.info("Student deleted successfully.");
            } else {
                log.error("Student not found.");
                throw new InvalidDataException("Student not found.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Thread interrupted while trying to acquire lock.");
            throw new RuntimeException("Thread was interrupted while trying to acquire lock.");
        } catch (InvalidDataException e) {
            log.error(e.getMessage());
            throw new InvalidDataException(e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting student.");
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

            StudentResponse studentResponse = modelMapper.map(existingStudent, StudentResponse.class);
            updateStudentFields(existingStudent, updatedStudent, studentResponse);
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

            studentResponse.setAddresses(savedAddressEntities.size() != 0 ? savedAddressEntities : addressRepository.findAllByStudentId(studentId));
            studentResponse.setIdentityDocument(savedIdentityDocument.getId() != null ? savedIdentityDocument : identityDocumentRepository.findByStudentId(studentId));

            redisTemplate.opsForValue().set("student:" + studentId, existingStudent);

            log.info("Student updated successfully.");
            return studentResponse;
        }
        catch (InterruptedException e) {
            log.error("Thread interrupted while trying to acquire lock.");
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread was interrupted while trying to acquire lock.");
        }
        catch (InvalidDataException e) {
            log.error(e.getMessage());
            throw new InvalidDataException(e.getMessage());
        }
        catch (Exception e) {
            log.error("Error updating student.");
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
                        studentResponse.setDepartment(departmentRepository.findById(cachedStudent.getDepartment()).get().getName());
                        studentResponse.setProgram(programRepository.findById(cachedStudent.getProgram()).get().getName());
                        studentResponse.setStatus(studentStatusRepository.findById(cachedStudent.getStatus()).get().getName());
                        results.add(studentResponse);
                        return results;
                    }

                    StudentEntity student = studentRepository.findByStudentId(keyword);
                    if (student != null) {
                        StudentResponse studentResponse = modelMapper.map(student, StudentResponse.class);
                        studentResponse.setAddresses(addressRepository.findAllByStudentId(student.getStudentId()));
                        studentResponse.setIdentityDocument(identityDocumentRepository.findByStudentId(student.getStudentId()));
                        studentResponse.setDepartment(departmentRepository.findById(student.getDepartment()).get().getName());
                        studentResponse.setProgram(programRepository.findById(student.getProgram()).get().getName());
                        studentResponse.setStatus(studentStatusRepository.findById(student.getStatus()).get().getName());
                        redisTemplate.opsForValue().set("student:" + student.getStudentId(), student);
                        results.add(studentResponse);
                    }
                } else {
                    throw new RuntimeException("Cannot acquire lock for student search: " + keyword);
                }
            } catch (InterruptedException e) {
                log.error("Thread interrupted while searching student by id.");
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread interrupted while searching student.");
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }

            log.info("Student found by id: " + keyword);
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
                            studentResponse.setDepartment(departmentRepository.findById(student.getDepartment()).get().getName());
                            studentResponse.setProgram(programRepository.findById(student.getProgram()).get().getName());
                            studentResponse.setStatus(studentStatusRepository.findById(student.getStatus()).get().getName());
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
                        studentResponse.setDepartment(departmentRepository.findById(student.getDepartment()).get().getName());
                        studentResponse.setProgram(programRepository.findById(student.getProgram()).get().getName());
                        studentResponse.setStatus(studentStatusRepository.findById(student.getStatus()).get().getName());
                        results.add(studentResponse);
                        redisTemplate.opsForValue().set("student:" + student.getStudentId(), student);
                    }
                }
            } else {
                throw new RuntimeException("Cannot acquire lock for student name search: " + keyword);
            }
        } catch (InterruptedException e) {
            log.error("Thread interrupted while searching students by name.");
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread interrupted while searching students by name.");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

        log.info("Students found by name: " + keyword);
        return results;
    }

    public List<StudentResponse> searchStudentsByDepartmentAndName(String keyword, String department) {
        List<StudentResponse> results = new ArrayList<>();

        if (department != null && !department.isEmpty()) {
            List<StudentEntity> students;
            DepartmentEntity departmentEntity = departmentRepository.findByName(department);

            if (keyword != null && !keyword.isEmpty()) {
                students = studentRepository.findByDepartmentAndFullNameContainingIgnoreCase(departmentEntity.getId(), keyword);
            } else {
                students = studentRepository.findByDepartment(departmentEntity.getId());
            }

            for (StudentEntity student : students) {
                StudentResponse studentResponse = modelMapper.map(student, StudentResponse.class);
                studentResponse.setAddresses(addressRepository.findAllByStudentId(student.getStudentId()));
                studentResponse.setIdentityDocument(identityDocumentRepository.findByStudentId(student.getStudentId()));
                studentResponse.setDepartment(departmentRepository.findById(student.getDepartment()).get().getName());
                studentResponse.setProgram(programRepository.findById(student.getProgram()).get().getName());
                studentResponse.setStatus(studentStatusRepository.findById(student.getStatus()).get().getName());
                results.add(studentResponse);
            }
        }

        log.info("Students found by department: " + department);
        return results;
    }

    public void updateStudentFields(StudentEntity existingStudent, StudentUpdateRequest updatedStudent, StudentResponse studentResponse) {
        if (updatedStudent.getFullName() != null) {
            existingStudent.setFullName(updatedStudent.getFullName());
            studentResponse.setFullName(updatedStudent.getFullName());
        }
        if (updatedStudent.getBirthday() != null) {
            existingStudent.setBirthday(updatedStudent.getBirthday());
            studentResponse.setBirthday(updatedStudent.getBirthday());
        }
        if (updatedStudent.getGender() != null) {
            existingStudent.setGender(updatedStudent.getGender());
            studentResponse.setGender(updatedStudent.getGender());
        }
        DepartmentEntity departmentEntity = departmentRepository.findByName(updatedStudent.getDepartment());
        if (updatedStudent.getDepartment() != null && departmentEntity != null) {
            existingStudent.setDepartment(departmentEntity.getId());
            studentResponse.setDepartment(departmentEntity.getName());
        }
        if (updatedStudent.getCourse() != null) {
            existingStudent.setCourse(updatedStudent.getCourse());
            studentResponse.setCourse(updatedStudent.getCourse());
        }
        ProgramEntity programEntity = programRepository.findByName(updatedStudent.getProgram());
        if (updatedStudent.getProgram() != null && programEntity != null) {
            existingStudent.setProgram(programEntity.getId());
            studentResponse.setProgram(programEntity.getName());
        }

        if (updatedStudent.getPhoneNumber() != null) {
            existingStudent.setPhoneNumber(updatedStudent.getPhoneNumber());
            studentResponse.setPhoneNumber(updatedStudent.getPhoneNumber());
        }
        StudentStatusEntity studentStatusEntity = studentStatusRepository.findByName(updatedStudent.getStatus());
        if (updatedStudent.getStatus() != null && studentStatusEntity != null) {
            existingStudent.setStatus(studentStatusEntity.getId());
            studentResponse.setStatus(studentStatusEntity.getName());
        }
    }


}
