package com.devnest.course.repository.quiz;

import com.devnest.course.entity.quiz.QuizAnswer;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizAnswerRepository extends JpaRepository<QuizAnswer, UUID> {

	List<QuizAnswer> findAllByAttemptIdOrderByQuestionPositionAsc(UUID attemptId);

	void deleteAllByAttemptQuizLessonModuleCourseId(UUID courseId);
}

