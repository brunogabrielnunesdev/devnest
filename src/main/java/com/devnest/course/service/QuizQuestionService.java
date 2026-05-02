package com.devnest.course.service;

import com.devnest.course.domain.Quiz;
import com.devnest.course.domain.QuizQuestion;
import com.devnest.course.dto.QuizQuestionResponse;
import com.devnest.course.mapper.QuizQuestionMapper;
import com.devnest.course.repository.QuizOptionRepository;
import com.devnest.course.repository.QuizQuestionRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QuizQuestionService {

	private final CourseAuthoringAccessService accessService;
	private final QuizQuestionMapper quizQuestionMapper;
	private final QuizQuestionRepository quizQuestionRepository;
	private final QuizOptionRepository quizOptionRepository;

	@Transactional
	public QuizQuestionResponse create(UUID courseId, UUID moduleId, UUID lessonId, QuizQuestion question) {
		Quiz quiz = accessService.getOwnedQuiz(courseId, moduleId, lessonId);
		question.setQuiz(quiz);

		return quizQuestionMapper.toResponse(quizQuestionRepository.save(question));
	}

	@Transactional(readOnly = true)
	public List<QuizQuestionResponse> findAll(UUID courseId, UUID moduleId, UUID lessonId) {
		Quiz quiz = accessService.getOwnedQuiz(courseId, moduleId, lessonId);

		return quizQuestionRepository.findAllByQuizIdOrderByPositionAsc(quiz.getId())
			.stream()
			.map(quizQuestionMapper::toResponse)
			.toList();
	}

	@Transactional(readOnly = true)
	public QuizQuestionResponse findById(UUID courseId, UUID moduleId, UUID lessonId, UUID questionId) {
		return quizQuestionMapper.toResponse(accessService.getOwnedQuestion(courseId, moduleId, lessonId, questionId));
	}

	@Transactional
	public QuizQuestionResponse update(UUID courseId, UUID moduleId, UUID lessonId, UUID questionId, QuizQuestion questionData) {
		QuizQuestion question = accessService.getOwnedQuestion(courseId, moduleId, lessonId, questionId);
		question.setStatement(questionData.getStatement());
		question.setPosition(questionData.getPosition());

		return quizQuestionMapper.toResponse(question);
	}

	@Transactional
	public void delete(UUID courseId, UUID moduleId, UUID lessonId, UUID questionId) {
		QuizQuestion question = accessService.getOwnedQuestion(courseId, moduleId, lessonId, questionId);
		quizOptionRepository.deleteAllByQuestionId(question.getId());
		quizQuestionRepository.delete(question);
	}
}
