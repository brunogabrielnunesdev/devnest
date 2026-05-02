package com.devnest.course.service;

import com.devnest.course.domain.Lesson;
import com.devnest.course.domain.Quiz;
import com.devnest.course.dto.QuizResponse;
import com.devnest.course.mapper.QuizMapper;
import com.devnest.course.repository.QuizOptionRepository;
import com.devnest.course.repository.QuizQuestionRepository;
import com.devnest.course.repository.QuizRepository;
import com.devnest.shared.exception.ConflictException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QuizService {

	private final CourseAuthoringAccessService accessService;
	private final QuizMapper quizMapper;
	private final QuizRepository quizRepository;
	private final QuizQuestionRepository quizQuestionRepository;
	private final QuizOptionRepository quizOptionRepository;

	@Transactional
	public QuizResponse create(UUID courseId, UUID moduleId, UUID lessonId, Quiz quiz) {
		Lesson lesson = accessService.getOwnedLesson(courseId, moduleId, lessonId);

		if (quizRepository.existsByLessonId(lesson.getId())) {
			throw new ConflictException("Lesson already has a quiz.");
		}

		quiz.setLesson(lesson);
		return quizMapper.toResponse(quizRepository.save(quiz));
	}

	@Transactional(readOnly = true)
	public QuizResponse findById(UUID courseId, UUID moduleId, UUID lessonId) {
		return quizMapper.toResponse(accessService.getOwnedQuiz(courseId, moduleId, lessonId));
	}

	@Transactional
	public QuizResponse update(UUID courseId, UUID moduleId, UUID lessonId, Quiz quizData) {
		Quiz quiz = accessService.getOwnedQuiz(courseId, moduleId, lessonId);
		quiz.setTitle(quizData.getTitle());
		quiz.setPassingScore(quizData.getPassingScore());
		quiz.setMaxAttempts(quizData.getMaxAttempts());
		quiz.setMaxQuestions(quizData.getMaxQuestions());

		return quizMapper.toResponse(quiz);
	}

	@Transactional
	public void delete(UUID courseId, UUID moduleId, UUID lessonId) {
		Quiz quiz = accessService.getOwnedQuiz(courseId, moduleId, lessonId);

		for (var question : quizQuestionRepository.findAllByQuizIdOrderByPositionAsc(quiz.getId())) {
			quizOptionRepository.deleteAllByQuestionId(question.getId());
		}

		quizQuestionRepository.deleteAllByQuizId(quiz.getId());
		quizRepository.delete(quiz);
	}
}
