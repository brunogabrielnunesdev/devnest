package com.devnest.course.repository;

import com.devnest.course.domain.Course;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, UUID> {

	List<Course> findAllByTeacherIdOrderByCreatedAtDesc(UUID teacherId);

	Optional<Course> findByIdAndTeacherId(UUID id, UUID teacherId);
}
