package com.devnest.course.service.course;

import com.devnest.auth.security.useridentity.CustomUserProvider;
import com.devnest.course.entity.course.Course;
import com.devnest.course.entity.course.CourseStatus;
import com.devnest.course.dto.course.CourseResponse;
import com.devnest.common.exception.ConflictException;
import com.devnest.course.mapper.course.CourseMapper;
import com.devnest.course.repository.module.ModuleRepository;
import com.devnest.course.repository.course.CourseRepository;
import com.devnest.course.repository.lesson.LessonRepository;
import com.devnest.common.exception.ForbiddenException;
import com.devnest.common.exception.ResourceNotFoundException;
import com.devnest.identity.entity.User;
import com.devnest.identity.entity.UserRole;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class CourseService {

	private final CustomUserProvider customUserProvider;
	private final CourseMapper courseMapper;
	private final CourseRepository courseRepository;
	private final ModuleRepository moduleRepository;
	private final LessonRepository lessonRepository;

	@Transactional
	public CourseResponse create(Course course) {
		User teacher = getAuthenticatedTeacher();
		validateTeacher(teacher);

		course.setTeacher(teacher);
		course.setStatus(CourseStatus.DRAFT);
		course.setArchived(false);

		return courseMapper.toResponse(courseRepository.save(course));
	}

	@Transactional(readOnly = true)
	public List<CourseResponse> findAll() {
		User teacher = getAuthenticatedTeacher();
		validateTeacher(teacher);
		return courseRepository.findAllByTeacherIdOrderByCreatedAtDesc(teacher.getId())
			.stream()
			.map(courseMapper::toResponse)
			.toList();
	}

	@Transactional(readOnly = true)
	public CourseResponse findById(UUID courseId) {
		User teacher = getAuthenticatedTeacher();
		validateTeacher(teacher);

		return courseMapper.toResponse(findOwnedCourse(teacher, courseId));
	}

	@Transactional
	public CourseResponse update(UUID courseId, Course courseData) {
		User teacher = getAuthenticatedTeacher();
		validateTeacher(teacher);

		Course course = findOwnedCourse(teacher, courseId);
		course.update(courseData.getTitle(), courseData.getDescription(), courseData.getLevel(), courseData.getCoverImageUrl());

		return courseMapper.toResponse(course);
	}

	@Transactional
	public CourseResponse publish(UUID courseId) {
		User teacher = getAuthenticatedTeacher();
		validateTeacher(teacher);

		Course course = findOwnedCourse(teacher, courseId);
		validateCourseCanBePublished(course);
		course.setStatus(CourseStatus.PUBLISHED);

		return courseMapper.toResponse(course);
	}

	@Transactional(readOnly = true)
	public List<CourseResponse> findPublishedCourses() {
		return courseRepository.findAllByStatusOrderByCreatedAtDesc(CourseStatus.PUBLISHED)
			.stream()
			.map(courseMapper::toResponse)
			.toList();
	}

	@Transactional(readOnly = true)
	public CourseResponse findPublishedCourseById(UUID courseId) {
		return courseRepository.findByIdAndStatusAndArchivedFalse(courseId, CourseStatus.PUBLISHED)
			.map(courseMapper::toResponse)
			.orElseThrow(() -> new ResourceNotFoundException("Course not found."));
	}

	@Transactional
	public void delete(UUID courseId) {
		User teacher = getAuthenticatedTeacher();
		validateTeacher(teacher);

		Course course = findOwnedCourse(teacher, courseId);
		course.archive();
	}

	private User getAuthenticatedTeacher() {
		return customUserProvider.getAuthenticatedUser();
	}

	private Course findOwnedCourse(User teacher, UUID courseId) {
		return courseRepository.findByIdAndTeacherId(courseId, teacher.getId())
			.orElseThrow(() -> new ResourceNotFoundException("Course not found."));
	}

	private void validateTeacher(User user) {
		if (user.getRole() != UserRole.TEACHER) {
			throw new ForbiddenException("Only teachers can manage courses.");
		}
	}

	private void validateCourseCanBePublished(Course course) {
		if (course.getStatus() == CourseStatus.PUBLISHED) {
			throw new ConflictException("Course is already published.");
		}
		if (course.isArchived()) {
			throw new ConflictException("Archived courses cannot be published.");
		}
		if (!moduleRepository.existsByCourseIdAndPosition(course.getId(), 1)) {
			throw new ConflictException("Course must have at least one module before publishing.");
		}
		if (lessonRepository.countByModuleCourseId(course.getId()) == 0) {
			throw new ConflictException("Course must have at least one lesson before publishing.");
		}
	}
}
