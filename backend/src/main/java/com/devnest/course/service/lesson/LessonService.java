package com.devnest.course.service.lesson;

import com.devnest.course.entity.module.Module;
import com.devnest.course.entity.lesson.Lesson;
import com.devnest.course.dto.lesson.LessonResponse;
import com.devnest.course.mapper.lesson.LessonMapper;
import com.devnest.course.repository.lesson.LessonRepository;
import com.devnest.course.repository.quiz.QuizRepository;
import com.devnest.common.exception.ConflictException;
import java.util.List;
import java.util.UUID;

import com.devnest.course.service.course.CourseAuthoringAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LessonService {

	private final CourseAuthoringAccessService accessService;
	private final LessonMapper lessonMapper;
	private final LessonRepository lessonRepository;
	private final QuizRepository quizRepository;

	@Transactional
	public LessonResponse create(UUID courseId, UUID moduleId, Lesson lesson) {
		Module module = accessService.getOwnedModule(courseId, moduleId);
		validateLessonPositionIsAvailable(module.getId(), lesson.getPosition());
		lesson.setModule(module);

		return lessonMapper.toResponse(lessonRepository.save(lesson));
	}

	@Transactional(readOnly = true)
	public List<LessonResponse> findAll(UUID courseId, UUID moduleId) {
		accessService.getOwnedModule(courseId, moduleId);

		return lessonRepository.findAllByModuleIdOrderByPositionAsc(moduleId)
			.stream()
			.map(lessonMapper::toResponse)
			.toList();
	}

	@Transactional(readOnly = true)
	public LessonResponse findById(UUID courseId, UUID moduleId, UUID lessonId) {
		return lessonMapper.toResponse(accessService.getOwnedLesson(courseId, moduleId, lessonId));
	}

	@Transactional
	public LessonResponse update(UUID courseId, UUID moduleId, UUID lessonId, Lesson lessonData) {
		Lesson lesson = accessService.getOwnedLesson(courseId, moduleId, lessonId);
		validateLessonPositionIsAvailableForUpdate(lesson.getModule().getId(), lessonData.getPosition(), lesson.getId());
		lesson.setTitle(lessonData.getTitle());
		lesson.setDescription(lessonData.getDescription());
		lesson.setContent(lessonData.getContent());
		lesson.setVideoUrl(lessonData.getVideoUrl());
		lesson.setPosition(lessonData.getPosition());

		return lessonMapper.toResponse(lesson);
	}

	@Transactional
	public void delete(UUID courseId, UUID moduleId, UUID lessonId) {
		Lesson lesson = accessService.getOwnedLesson(courseId, moduleId, lessonId);

		if (quizRepository.existsByLessonId(lesson.getId())) {
			throw new ConflictException("Lesson has a quiz and cannot be deleted.");
		}

		lessonRepository.delete(lesson);
	}

	private void validateLessonPositionIsAvailable(UUID moduleId, Integer position) {
		if (lessonRepository.existsByModuleIdAndPosition(moduleId, position)) {
			throw new ConflictException("Lesson position is already in use for this module.");
		}
	}

	private void validateLessonPositionIsAvailableForUpdate(UUID moduleId, Integer position, UUID lessonId) {
		if (lessonRepository.existsByModuleIdAndPositionAndIdNot(moduleId, position, lessonId)) {
			throw new ConflictException("Lesson position is already in use for this module.");
		}
	}
}

