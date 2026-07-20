package com.devnest.course.repository.question;

import com.devnest.course.entity.quiz.QuizQuestion;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<QuizQuestion, UUID> {

	List<QuizQuestion> findAllByQuizIdOrderByPositionAsc(UUID quizId);

	boolean existsByQuizId(UUID quizId);

	long countByQuizId(UUID quizId);

	boolean existsByQuizIdAndPosition(UUID quizId, Integer position);

	boolean existsByQuizIdAndPositionAndIdNot(UUID quizId, Integer position, UUID id);

	void deleteAllByQuizId(UUID quizId);

	void deleteAllByQuizLessonModuleCourseId(UUID courseId);
}

