package com.devnest.course.service.option;

import com.devnest.course.dto.option.OptionResponse;
import com.devnest.course.entity.quiz.option.Option;
import com.devnest.course.entity.quiz.QuizQuestion;
import com.devnest.course.mapper.option.OptionMapper;
import com.devnest.course.repository.option.OptionRepository;
import com.devnest.common.exception.ConflictException;
import java.util.List;
import java.util.UUID;

import com.devnest.course.service.course.CourseAuthoringAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OptionService {

	private final CourseAuthoringAccessService accessService;
	private final OptionMapper optionMapper;
	private final OptionRepository optionRepository;

	@Transactional
	public OptionResponse create(UUID courseId, UUID moduleId, UUID lessonId, UUID questionId, Option option) {
		QuizQuestion question = accessService.getOwnedQuestion(courseId, moduleId, lessonId, questionId);
		validateOptionPositionIsAvailable(question.getId(), option.getPosition());
		option.setQuestion(question);

		return optionMapper.toResponse(optionRepository.save(option));
	}

	@Transactional(readOnly = true)
	public List<OptionResponse> findAll(UUID courseId, UUID moduleId, UUID lessonId, UUID questionId) {
		QuizQuestion question = accessService.getOwnedQuestion(courseId, moduleId, lessonId, questionId);

		return optionRepository.findAllByQuestionIdOrderByPositionAsc(question.getId())
			.stream()
			.map(optionMapper::toResponse)
			.toList();
	}

	@Transactional(readOnly = true)
	public OptionResponse findById(UUID courseId, UUID moduleId, UUID lessonId, UUID questionId, UUID optionId) {
		return optionMapper.toResponse(accessService.getOwnedOption(courseId, moduleId, lessonId, questionId, optionId));
	}

	@Transactional
	public OptionResponse update(UUID courseId, UUID moduleId, UUID lessonId, UUID questionId, UUID optionId, Option optionData) {
		Option option = accessService.getOwnedOption(courseId, moduleId, lessonId, questionId, optionId);
		validateOptionPositionIsAvailableForUpdate(option.getQuestion().getId(), optionData.getPosition(), option.getId());
		option.setText(optionData.getText());
		option.setCorrect(optionData.getCorrect());
		option.setPosition(optionData.getPosition());

		return optionMapper.toResponse(option);
	}

	@Transactional
	public void delete(UUID courseId, UUID moduleId, UUID lessonId, UUID questionId, UUID optionId) {
		Option option = accessService.getOwnedOption(courseId, moduleId, lessonId, questionId, optionId);
		optionRepository.delete(option);
	}

	private void validateOptionPositionIsAvailable(UUID questionId, Integer position) {
		if (optionRepository.existsByQuestionIdAndPosition(questionId, position)) {
			throw new ConflictException("Option position is already in use for this question.");
		}
	}

	private void validateOptionPositionIsAvailableForUpdate(UUID questionId, Integer position, UUID optionId) {
		if (optionRepository.existsByQuestionIdAndPositionAndIdNot(questionId, position, optionId)) {
			throw new ConflictException("Option position is already in use for this question.");
		}
	}
}

