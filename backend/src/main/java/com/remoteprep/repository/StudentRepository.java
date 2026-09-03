package com.remoteprep.repository;

import com.remoteprep.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA Repository for Student entity.
 */
@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    /**
     * Finds a student by roll number.
     * Used to identify existing candidates and prevent duplicate records.
     */
    Optional<Student> findByRollNumber(String rollNumber);
}
