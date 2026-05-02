package com.devnest.course.service;

import com.devnest.course.domain.QuizOption;
import com.devnest.course.domain.QuizQuestion;
import com.devnest.course.dto.QuizOptionResponse;
import com.devnest.course.mapper.QuizOptionMapper;
import com.devnest.course.repository.QuizOptionRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QuizOptionService {

	private final CourseAuthoringAccessService accessService;
	private final QuizOptionMapper quizOptionMapper;
	private final QuizOptionRepository quizOptionRepository;

	@Transactional
	public QuizOptionResponse create(UUID courseId, UUID moduleId, UUID lessonId, UUID questionId, QuizOption option) {
		QuizQuestion question = accessService.getOwnedQuestion(courseId, moduleId, lessonId, questionId);
		option.setQuestion(question);

		return quizOptionMapper.toResponse(quizOptionRepository.save(option));
	}

	@Transactional(readOnly = true)
	public List<QuizOptionResponse> findAll(UUID courseId, UUID moduleId, UUID lessonId, UUID questionId) {
		QuizQuestion question = accessService.getOwnedQuestion(courseId, moduleId, lessonId, questionId);

		return quizOptionRepository.findAllByQuestionIdOrderByPositionAsc(question.getId())
			.stream()
			.map(quizOptionMapper::toResponse)
			.toList();
	}

	@Transactional(readOnly = true)
	public QuizOptionResponse findById(UUID courseId, UUID moduleId, UUID lessonId, UUID questionId, UUID optionId) {
		return quizOptionMapper.toResponse(accessService.getOwnedOption(courseId, moduleId, lessonId, questionId, optionId));
	}

	@Transactional
	public QuizOptionResponse update(UUID courseId, UUID moduleId, UUID lessonId, UUID questionId, UUID optionId, QuizOption optionData) {
		QuizOption option = accessService.getOwnedOption(courseId, moduleId, lessonId, questionId, optionId);
		option.setText(optionData.getText());
		option.setCorrect(optionData.getCorrect());
		option.setPosition(optionData.getPosition());

		return quizOptionMapper.toResponse(option);
	}

	@Transactional
	public void delete(UUID courseId, UUID moduleId, UUID lessonId, UUID questionId, UUID optionId) {
		QuizOption option = accessService.getOwnedOption(courseId, moduleId, lessonId, questionId, optionId);
		quizOptionRepository.delete(option);
	}
}
