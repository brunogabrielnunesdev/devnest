package com.devnest.course.service;

import com.devnest.course.domain.Course;
import com.devnest.course.domain.CourseModule;
import com.devnest.course.dto.CourseModuleResponse;
import com.devnest.course.mapper.CourseModuleMapper;
import com.devnest.course.repository.CourseModuleRepository;
import com.devnest.course.repository.LessonRepository;
import com.devnest.shared.exception.ConflictException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseModuleService {

	private final CourseAuthoringAccessService accessService;
	private final CourseModuleMapper courseModuleMapper;
	private final CourseModuleRepository courseModuleRepository;
	private final LessonRepository lessonRepository;

	@Transactional
	public CourseModuleResponse create(UUID courseId, CourseModule module) {
		Course course = accessService.getOwnedCourse(courseId);
		module.setCourse(course);

		return courseModuleMapper.toResponse(courseModuleRepository.save(module));
	}

	@Transactional(readOnly = true)
	public List<CourseModuleResponse> findAll(UUID courseId) {
		accessService.getOwnedCourse(courseId);

		return courseModuleRepository.findAllByCourseIdOrderByPositionAsc(courseId)
			.stream()
			.map(courseModuleMapper::toResponse)
			.toList();
	}

	@Transactional(readOnly = true)
	public CourseModuleResponse findById(UUID courseId, UUID moduleId) {
		return courseModuleMapper.toResponse(accessService.getOwnedModule(courseId, moduleId));
	}

	@Transactional
	public CourseModuleResponse update(UUID courseId, UUID moduleId, CourseModule moduleData) {
		CourseModule module = accessService.getOwnedModule(courseId, moduleId);
		module.setTitle(moduleData.getTitle());
		module.setDescription(moduleData.getDescription());
		module.setPosition(moduleData.getPosition());

		return courseModuleMapper.toResponse(module);
	}

	@Transactional
	public void delete(UUID courseId, UUID moduleId) {
		CourseModule module = accessService.getOwnedModule(courseId, moduleId);

		if (lessonRepository.existsByModuleId(module.getId())) {
			throw new ConflictException("Module has lessons and cannot be deleted.");
		}

		courseModuleRepository.delete(module);
	}
}
