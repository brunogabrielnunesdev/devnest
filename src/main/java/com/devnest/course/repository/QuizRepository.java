package com.devnest.course.repository;

import com.devnest.course.domain.Quiz;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<Quiz, UUID> {

	Optional<Quiz> findByLessonId(UUID lessonId);

	boolean existsByLessonId(UUID lessonId);
}
