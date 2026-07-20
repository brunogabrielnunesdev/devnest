package com.devnest.course.repository.module;

import com.devnest.course.entity.module.Module;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ModuleRepository extends JpaRepository<Module, UUID> {

	List<Module> findAllByCourseIdOrderByPositionAsc(UUID courseId);

	long countByCourseTeacherId(UUID teacherId);

	boolean existsByCourseIdAndPosition(UUID courseId, Integer position);

	boolean existsByCourseIdAndPositionAndIdNot(UUID courseId, Integer position, UUID id);

	void deleteAllByCourseId(UUID courseId);
}

