package com.bookingsystem.repository;

import com.bookingsystem.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    boolean existsByTitleIgnoreCase(String title);
    Optional<Course> findByTitleIgnoreCase(String title);
}
