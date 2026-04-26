package com.devnest.course;

import com.devnest.auth.security.AuthenticatedUserProvider;
import com.devnest.course.domain.Course;
import com.devnest.course.domain.CourseStatus;
import com.devnest.course.dto.CourseResponse;
import com.devnest.course.mapper.CourseMapper;
import com.devnest.course.repository.CourseRepository;
import com.devnest.shared.exception.ForbiddenException;
import com.devnest.shared.exception.ResourceNotFoundException;
import com.devnest.user.domain.User;
import com.devnest.user.domain.UserRole;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseService {

	private final AuthenticatedUserProvider authenticatedUserProvider;
	private final CourseMapper courseMapper;
	private final CourseRepository courseRepository;

	@Transactional
	public CourseResponse create(Course course) {
		User teacher = getAuthenticatedTeacher();
		validateTeacher(teacher);

		course.setTeacher(teacher);
		course.setStatus(CourseStatus.DRAFT);

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
		course.update(courseData.getTitle(), courseData.getDescription(), courseData.getLevel());

		return courseMapper.toResponse(course);
	}

	@Transactional
	public void delete(UUID courseId) {
		User teacher = getAuthenticatedTeacher();
		validateTeacher(teacher);

		Course course = findOwnedCourse(teacher, courseId);
		course.archive();
	}

	private User getAuthenticatedTeacher() {
		return authenticatedUserProvider.getAuthenticatedUser();
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
}
