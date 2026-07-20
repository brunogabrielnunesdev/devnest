package com.devnest.course.repository.lesson;

import com.devnest.course.entity.lesson.LessonProgress;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonProgressRepository extends JpaRepository<LessonProgress, UUID> {

	Optional<LessonProgress> findByStudentIdAndLessonId(UUID studentId, UUID lessonId);

	List<LessonProgress> findAllByStudentIdAndLessonIdIn(UUID studentId, List<UUID> lessonIds);

	long countByStudentIdAndLessonModuleCourseIdAndCompletedTrue(UUID studentId, UUID courseId);

	long countByStudentIdAndCompletedTrue(UUID studentId);

	void deleteAllByLessonModuleCourseId(UUID courseId);
}

