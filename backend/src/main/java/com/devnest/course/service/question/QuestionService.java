package com.devnest.course.service.question;

import com.devnest.course.dto.question.QuestionResponse;
import com.devnest.course.entity.quiz.Quiz;
import com.devnest.course.entity.quiz.QuizQuestion;
import com.devnest.course.mapper.question.QuestionMapper;
import com.devnest.course.repository.option.OptionRepository;
import com.devnest.course.repository.question.QuestionRepository;
import com.devnest.common.exception.ConflictException;
import java.util.List;
import java.util.UUID;

import com.devnest.course.service.course.CourseAuthoringAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QuestionService {

	private final CourseAuthoringAccessService accessService;
	private final QuestionMapper questionMapper;
	private final QuestionRepository questionRepository;
	private final OptionRepository optionRepository;

	@Transactional
	public QuestionResponse create(UUID courseId, UUID moduleId, UUID lessonId, QuizQuestion question) {
		Quiz quiz = accessService.getOwnedQuiz(courseId, moduleId, lessonId);
		validateQuestionCreation(quiz, question.getPosition());
		question.setQuiz(quiz);

		return questionMapper.toResponse(questionRepository.save(question));
	}

	@Transactional(readOnly = true)
	public List<QuestionResponse> findAll(UUID courseId, UUID moduleId, UUID lessonId) {
		Quiz quiz = accessService.getOwnedQuiz(courseId, moduleId, lessonId);

		return questionRepository.findAllByQuizIdOrderByPositionAsc(quiz.getId())
			.stream()
			.map(questionMapper::toResponse)
			.toList();
	}

	@Transactional(readOnly = true)
	public QuestionResponse findById(UUID courseId, UUID moduleId, UUID lessonId, UUID questionId) {
		return questionMapper.toResponse(accessService.getOwnedQuestion(courseId, moduleId, lessonId, questionId));
	}

	@Transactional
	public QuestionResponse update(UUID courseId, UUID moduleId, UUID lessonId, UUID questionId, QuizQuestion questionData) {
		QuizQuestion question = accessService.getOwnedQuestion(courseId, moduleId, lessonId, questionId);
		validateQuestionUpdate(question.getQuiz(), questionData.getPosition(), question.getId());
		question.setStatement(questionData.getStatement());
		question.setPosition(questionData.getPosition());

		return questionMapper.toResponse(question);
	}

	@Transactional
	public void delete(UUID courseId, UUID moduleId, UUID lessonId, UUID questionId) {
		QuizQuestion question = accessService.getOwnedQuestion(courseId, moduleId, lessonId, questionId);
		optionRepository.deleteAllByQuestionId(question.getId());
		questionRepository.delete(question);
	}

	private void validateQuestionCreation(Quiz quiz, Integer position) {
		if (position > quiz.getMaxQuestions()) {
			throw new ConflictException("Question position exceeds the quiz limit.");
		}

		if (questionRepository.countByQuizId(quiz.getId()) >= quiz.getMaxQuestions()) {
			throw new ConflictException("Quiz already reached the maximum number of questions.");
		}

		if (questionRepository.existsByQuizIdAndPosition(quiz.getId(), position)) {
			throw new ConflictException("Question position is already in use for this quiz.");
		}
	}

	private void validateQuestionUpdate(Quiz quiz, Integer position, UUID questionId) {
		if (position > quiz.getMaxQuestions()) {
			throw new ConflictException("Question position exceeds the quiz limit.");
		}

		if (questionRepository.existsByQuizIdAndPositionAndIdNot(quiz.getId(), position, questionId)) {
			throw new ConflictException("Question position is already in use for this quiz.");
		}
	}
}

