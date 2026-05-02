package com.devnest.course.repository;

import com.devnest.course.domain.CourseModule;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseModuleRepository extends JpaRepository<CourseModule, UUID> {

	List<CourseModule> findAllByCourseIdOrderByPositionAsc(UUID courseId);
}
