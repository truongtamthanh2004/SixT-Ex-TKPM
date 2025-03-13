package com.example.sixt.services.impl;

import com.example.sixt.controllers.requests.StudentCreationRequest;
import com.example.sixt.controllers.requests.StudentUpdateRequest;
import com.example.sixt.exceptions.InvalidDataException;
import com.example.sixt.models.StudentEntity;
import com.example.sixt.repositories.StudentRepository;
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

@Service
public class StudentServiceImpl implements StudentService {
    private final StudentRepository studentRepository;
    private final ModelMapper modelMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedissonClient redissonClient;

    @Autowired
    public StudentServiceImpl(StudentRepository studentRepository,
                              ModelMapper modelMapper,
                              RedisTemplate<String, Object> redisTemplate,
                              RedissonClient redissonClient) {
        this.studentRepository = studentRepository;
        this.modelMapper = modelMapper;
        this.redisTemplate = redisTemplate;
        this.redissonClient = redissonClient;
    }

    @Override
    @Transactional
    @CacheEvict(value = "students", key = "#student.studentId")
    public StudentEntity addStudent(StudentCreationRequest student) {
        RLock lock = redissonClient.getReadWriteLock("lock:student:" + student.getStudentId()).writeLock();
        boolean isLocked = false;

        try {
            isLocked = lock.tryLock(0, 500, TimeUnit.SECONDS);
            if (!isLocked) {
                throw new RuntimeException("Error getting lock");
            }
            if (studentRepository.findByStudentId(student.getStudentId()) != null) {
                throw new InvalidDataException("Student id already exists.");
            }

            if (studentRepository.findByEmail(student.getEmail()) != null) {
                throw new InvalidDataException("Email already exists.");
            }

            StudentEntity studentEntity = modelMapper.map(student, StudentEntity.class);
            studentEntity.setId(null);

            StudentEntity savedStudent = studentRepository.save(studentEntity);
            redisTemplate.opsForValue().set("student:" + student.getStudentId(), savedStudent);
            lock.unlock();
            return savedStudent;
        }
        catch (InvalidDataException e) {
            throw new InvalidDataException(e.getMessage());
        }
        catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    @CacheEvict(value = "students", key = "#studentId")
    public void deleteStudent(String studentId) {
        RLock lock = redissonClient.getReadWriteLock("lock:student:" + studentId).writeLock();

        try {
            if (!lock.tryLock(0, 100, TimeUnit.SECONDS)) {
                throw new RuntimeException("Cannot acquire lock for student " + studentId);
            }

            StudentEntity student = studentRepository.findByStudentId(studentId);
            if (student != null) {
                studentRepository.delete(student);

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
    public StudentEntity updateStudent(String studentId, StudentUpdateRequest updatedStudent) {
        RLock lock = redissonClient.getReadWriteLock("lock:student:" + studentId).writeLock();

        try {
            if (!lock.tryLock(0, 100, TimeUnit.SECONDS)) {
                throw new RuntimeException("Cannot acquire lock for student " + studentId);
            }

            StudentEntity existingStudent = studentRepository.findByStudentId(studentId);
            if (existingStudent == null) {
                throw new InvalidDataException("Student not found.");
            }

            if (updatedStudent.getFullName() != null) {
                existingStudent.setFullName(updatedStudent.getFullName());
            }
            if (updatedStudent.getBirthday() != null) {
                existingStudent.setBirthday(updatedStudent.getBirthday());
            }
            if (updatedStudent.getGender() != null) {
                existingStudent.setGender(updatedStudent.getGender());
            }
            if (updatedStudent.getDepartment() != null) {
                existingStudent.setDepartment(updatedStudent.getDepartment());
            }
            if (updatedStudent.getCourse() != null) {
                existingStudent.setCourse(updatedStudent.getCourse());
            }
            if (updatedStudent.getProgram() != null) {
                existingStudent.setProgram(updatedStudent.getProgram());
            }
            if (updatedStudent.getAddress() != null) {
                existingStudent.setAddress(updatedStudent.getAddress());
            }
            if (updatedStudent.getPhoneNumber() != null) {
                existingStudent.setPhoneNumber(updatedStudent.getPhoneNumber());
            }
            if (updatedStudent.getStatus() != null) {
                existingStudent.setStatus(updatedStudent.getStatus());
            }

            redisTemplate.opsForValue().set("student:" + studentId, existingStudent);
            return studentRepository.save(existingStudent);
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
    public List<StudentEntity> searchStudents(String keyword) {
        List<StudentEntity> results = new ArrayList<>();

        if (keyword.matches("\\d+")) {
            RLock lock = redissonClient.getReadWriteLock("lock:student:" + keyword).readLock();

            try {
                if (lock.tryLock(0, 100, TimeUnit.SECONDS)) {
                    StudentEntity cachedStudent = (StudentEntity) redisTemplate.opsForValue().get("student:" + keyword);
                    if (cachedStudent != null) {
                        results.add(cachedStudent);
                        return results;
                    }

                    StudentEntity student = studentRepository.findByStudentId(keyword);
                    if (student != null) {
                        redisTemplate.opsForValue().set("student:" + student.getStudentId(), student);
                        results.add(student);
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
                            results.add(student);
                        }
                    }
                }

                if (results.isEmpty()) {
                    results = studentRepository.findByStudentIdOrFullName(keyword);
                    for (StudentEntity student : results) {
                        redisTemplate.opsForValue().set("student:" + student.getStudentId(), student);
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

}
