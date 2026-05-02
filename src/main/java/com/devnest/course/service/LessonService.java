package com.devnest.course.service;

import com.devnest.course.domain.CourseModule;
import com.devnest.course.domain.Lesson;
import com.devnest.course.dto.LessonResponse;
import com.devnest.course.mapper.LessonMapper;
import com.devnest.course.repository.LessonRepository;
import com.devnest.course.repository.QuizRepository;
import com.devnest.shared.exception.ConflictException;
import java.util.List;
import java.util.UUID;
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
		CourseModule module = accessService.getOwnedModule(courseId, moduleId);
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
}
