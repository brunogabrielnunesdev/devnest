package com.devnest.course.service.student;

import com.devnest.course.dto.student.learning.StudentCourseLearningContentResponse;
import com.devnest.course.dto.student.learning.StudentCourseLearningLessonResponse;
import com.devnest.course.dto.student.learning.StudentCourseLearningModuleResponse;
import com.devnest.course.repository.module.ModuleRepository;
import com.devnest.course.repository.lesson.LessonProgressRepository;
import com.devnest.course.repository.lesson.LessonRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.devnest.course.service.course.CourseEnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentLearningContentService {

	private final StudentLearningAccessService accessService;
	private final CourseEnrollmentService courseEnrollmentService;
	private final ModuleRepository moduleRepository;
	private final LessonRepository lessonRepository;
	private final LessonProgressRepository lessonProgressRepository;

	@Transactional(readOnly = true)
	public StudentCourseLearningContentResponse getLearningContent(UUID courseId) {
		var student = accessService.getAuthenticatedStudent();
		var course = accessService.getPublishedCourse(courseId);
		courseEnrollmentService.getActiveOrCompletedEnrollment(courseId);

		var modules = moduleRepository.findAllByCourseIdOrderByPositionAsc(courseId);
		var lessonsByModuleId = modules.stream()
			.collect(java.util.stream.Collectors.toMap(
				module -> module.getId(),
				module -> lessonRepository.findAllByModuleIdOrderByPositionAsc(module.getId())
			));

		var lessonIds = lessonsByModuleId.values().stream()
			.flatMap(List::stream)
			.map(lesson -> lesson.getId())
			.toList();

		Map<UUID, Boolean> completedByLessonId = lessonIds.isEmpty()
			? Map.of()
			: lessonProgressRepository.findAllByStudentIdAndLessonIdIn(student.getId(), lessonIds).stream()
				.collect(java.util.stream.Collectors.toMap(
					progress -> progress.getLesson().getId(),
					progress -> Boolean.TRUE.equals(progress.getCompleted()),
					(existing, replacement) -> existing
				));

		var responseModules = modules.stream()
			.map(module -> new StudentCourseLearningModuleResponse(
				module.getId(),
				module.getTitle(),
				module.getDescription(),
				module.getPosition(),
				lessonsByModuleId.getOrDefault(module.getId(), List.of()).stream()
					.map(lesson -> new StudentCourseLearningLessonResponse(
						lesson.getId(),
						lesson.getTitle(),
						lesson.getDescription(),
						lesson.getContent(),
						lesson.getVideoUrl(),
						lesson.getPosition(),
						completedByLessonId.getOrDefault(lesson.getId(), false)
					))
					.toList()
			))
			.toList();

		return new StudentCourseLearningContentResponse(
			course.getId(),
			course.getTitle(),
			course.getDescription(),
			course.getCoverImageUrl(),
			course.getStatus(),
			responseModules
		);
	}
}
