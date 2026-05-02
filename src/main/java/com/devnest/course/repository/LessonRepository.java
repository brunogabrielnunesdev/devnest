package com.devnest.course.repository;

import com.devnest.course.domain.Lesson;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonRepository extends JpaRepository<Lesson, UUID> {

	List<Lesson> findAllByModuleIdOrderByPositionAsc(UUID moduleId);

	boolean existsByModuleId(UUID moduleId);
}
