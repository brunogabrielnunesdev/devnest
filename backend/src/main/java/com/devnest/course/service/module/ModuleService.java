package com.devnest.course.service.module;

import com.devnest.course.dto.module.ModuleResponse;
import com.devnest.course.entity.module.Module;
import com.devnest.course.entity.course.Course;
import com.devnest.course.mapper.module.ModuleMapper;
import com.devnest.course.repository.module.ModuleRepository;
import com.devnest.course.repository.lesson.LessonRepository;
import com.devnest.common.exception.ConflictException;
import java.util.List;
import java.util.UUID;

import com.devnest.course.service.course.CourseAuthoringAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ModuleService {

	private final CourseAuthoringAccessService accessService;
	private final ModuleMapper moduleMapper;
	private final ModuleRepository moduleRepository;
	private final LessonRepository lessonRepository;

	@Transactional
	public ModuleResponse create(UUID courseId, Module module) {
		Course course = accessService.getOwnedCourse(courseId);
		validateModulePositionIsAvailable(course.getId(), module.getPosition());
		module.setCourse(course);

		return moduleMapper.toResponse(moduleRepository.save(module));
	}

	@Transactional(readOnly = true)
	public List<ModuleResponse> findAll(UUID courseId) {
		accessService.getOwnedCourse(courseId);

		return moduleRepository.findAllByCourseIdOrderByPositionAsc(courseId)
			.stream()
			.map(moduleMapper::toResponse)
			.toList();
	}

	@Transactional(readOnly = true)
	public ModuleResponse findById(UUID courseId, UUID moduleId) {
		return moduleMapper.toResponse(accessService.getOwnedModule(courseId, moduleId));
	}

	@Transactional
	public ModuleResponse update(UUID courseId, UUID moduleId, Module moduleData) {
		Module module = accessService.getOwnedModule(courseId, moduleId);
		validateModulePositionIsAvailableForUpdate(module.getCourse().getId(), moduleData.getPosition(), module.getId());
		module.setTitle(moduleData.getTitle());
		module.setDescription(moduleData.getDescription());
		module.setPosition(moduleData.getPosition());

		return moduleMapper.toResponse(module);
	}

	@Transactional
	public void delete(UUID courseId, UUID moduleId) {
		Module module = accessService.getOwnedModule(courseId, moduleId);

		if (lessonRepository.existsByModuleId(module.getId())) {
			throw new ConflictException("Module has lessons and cannot be deleted.");
		}

		moduleRepository.delete(module);
	}

	private void validateModulePositionIsAvailable(UUID courseId, Integer position) {
		if (moduleRepository.existsByCourseIdAndPosition(courseId, position)) {
			throw new ConflictException("Module position is already in use for this course.");
		}
	}

	private void validateModulePositionIsAvailableForUpdate(UUID courseId, Integer position, UUID moduleId) {
		if (moduleRepository.existsByCourseIdAndPositionAndIdNot(courseId, position, moduleId)) {
			throw new ConflictException("Module position is already in use for this course.");
		}
	}
}

