package com.devnest.course.repository.lesson;

import com.devnest.course.entity.lesson.Lesson;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonRepository extends JpaRepository<Lesson, UUID> {

	List<Lesson> findAllByModuleIdOrderByPositionAsc(UUID moduleId);

	long countByModuleCourseTeacherId(UUID teacherId);

	boolean existsByModuleId(UUID moduleId);

	long countByModuleCourseId(UUID courseId);

	boolean existsByModuleIdAndPosition(UUID moduleId, Integer position);

	boolean existsByModuleIdAndPositionAndIdNot(UUID moduleId, Integer position, UUID id);

	void deleteAllByModuleCourseId(UUID courseId);
}

