package com.devnest.course.repository.quiz;

import com.devnest.course.entity.quiz.Quiz;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<Quiz, UUID> {

	Optional<Quiz> findByLessonId(UUID lessonId);

	boolean existsByLessonId(UUID lessonId);

	long countByLessonModuleCourseTeacherId(UUID teacherId);

	void deleteAllByLessonModuleCourseId(UUID courseId);
}

