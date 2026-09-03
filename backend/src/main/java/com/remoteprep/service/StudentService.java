package com.remoteprep.service;

import com.remoteprep.entity.Student;
import com.remoteprep.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Layer for Student operations.
 * Manages student identity lookup and creation by roll number.
 */
@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    /**
     * Finds an existing student by roll number, or registers a new student if not found.
     * Guarantees that the same roll number reuses the same Student record and ID.
     */
    @Transactional
    public Student findOrCreateStudent(String name, String rollNumber) {
        return studentRepository.findByRollNumber(rollNumber.trim())
                .orElseGet(() -> {
                    Student newStudent = new Student(name.trim(), rollNumber.trim());
                    return studentRepository.save(newStudent);
                });
    }
}
