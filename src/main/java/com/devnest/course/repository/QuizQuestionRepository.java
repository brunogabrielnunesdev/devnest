package com.devnest.course.repository;

import com.devnest.course.domain.QuizQuestion;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, UUID> {

	List<QuizQuestion> findAllByQuizIdOrderByPositionAsc(UUID quizId);

	boolean existsByQuizId(UUID quizId);

	void deleteAllByQuizId(UUID quizId);
}
