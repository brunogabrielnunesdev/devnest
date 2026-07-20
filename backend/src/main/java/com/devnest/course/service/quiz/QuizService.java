package com.devnest.course.service.quiz;

import com.devnest.course.entity.lesson.Lesson;
import com.devnest.course.entity.quiz.Quiz;
import com.devnest.course.dto.quiz.QuizResponse;
import com.devnest.course.mapper.quiz.QuizMapper;
import com.devnest.course.repository.option.OptionRepository;
import com.devnest.course.repository.question.QuestionRepository;
import com.devnest.course.repository.quiz.QuizRepository;
import com.devnest.common.exception.ConflictException;
import java.util.UUID;

import com.devnest.course.service.course.CourseAuthoringAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QuizService {

	private final CourseAuthoringAccessService accessService;
	private final QuizMapper quizMapper;
	private final QuizRepository quizRepository;
	private final QuestionRepository questionRepository;
	private final OptionRepository optionRepository;

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

		for (var question : questionRepository.findAllByQuizIdOrderByPositionAsc(quiz.getId())) {
			optionRepository.deleteAllByQuestionId(question.getId());
		}

		questionRepository.deleteAllByQuizId(quiz.getId());
		quizRepository.delete(quiz);
	}
}

