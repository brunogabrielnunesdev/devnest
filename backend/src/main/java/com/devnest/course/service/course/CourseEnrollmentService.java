package com.devnest.course.service.course;

import com.devnest.common.exception.ConflictException;
import com.devnest.course.dto.course.enrollment.CourseEnrollmentResponse;
import com.devnest.course.entity.course.CourseEnrollment;
import com.devnest.course.entity.course.EnrollmentStatus;
import com.devnest.course.repository.course.CourseEnrollmentRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.devnest.course.service.student.StudentLearningAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseEnrollmentService {

	private final StudentLearningAccessService accessService;
	private final CourseEnrollmentRepository courseEnrollmentRepository;

	@Transactional
	public CourseEnrollmentResponse enroll(UUID courseId) {
		var student = accessService.getAuthenticatedStudent();
		var course = accessService.getPublishedCourse(courseId);

		if (course.getTeacher().getId().equals(student.getId())) {
			throw new ConflictException("Teacher cannot enroll in their own course.");
		}

		if (courseEnrollmentRepository.existsByStudentIdAndCourseId(student.getId(), course.getId())) {
			throw new ConflictException("Student is already enrolled in this course.");
		}

		CourseEnrollment enrollment = new CourseEnrollment();
		enrollment.setStudent(student);
		enrollment.setCourse(course);
		enrollment.setStatus(EnrollmentStatus.ACTIVE);
		enrollment.setEnrolledAt(OffsetDateTime.now());

		return toResponse(courseEnrollmentRepository.save(enrollment));
	}

	@Transactional(readOnly = true)
	public List<CourseEnrollmentResponse> findMyEnrollments() {
		var student = accessService.getAuthenticatedStudent();

		return courseEnrollmentRepository.findAllByStudentIdOrderByCreatedAtDesc(student.getId())
			.stream()
			.map(this::toResponse)
			.toList();
	}

	@Transactional(readOnly = true)
	public CourseEnrollmentResponse findMyEnrollment(UUID courseId) {
		var student = accessService.getAuthenticatedStudent();

		return toResponse(courseEnrollmentRepository.findByStudentIdAndCourseId(student.getId(), courseId)
			.orElseThrow(() -> new com.devnest.common.exception.ResourceNotFoundException("Enrollment not found.")));
	}

	public CourseEnrollment getActiveOrCompletedEnrollment(UUID courseId) {
		var student = accessService.getAuthenticatedStudent();
		CourseEnrollment enrollment = courseEnrollmentRepository.findByStudentIdAndCourseId(student.getId(), courseId)
			.orElseThrow(() -> new com.devnest.common.exception.ForbiddenException("Student is not enrolled in this course."));

		if (enrollment.getStatus() == EnrollmentStatus.CANCELLED) {
			throw new com.devnest.common.exception.ForbiddenException("Student is not enrolled in this course.");
		}

		return enrollment;
	}

	public void completeEnrollmentIfNeeded(CourseEnrollment enrollment) {
		if (enrollment.getStatus() != EnrollmentStatus.COMPLETED) {
			enrollment.setStatus(EnrollmentStatus.COMPLETED);
			enrollment.setCompletedAt(OffsetDateTime.now());
		}
	}

	private CourseEnrollmentResponse toResponse(CourseEnrollment enrollment) {
		return new CourseEnrollmentResponse(
			enrollment.getId(),
			enrollment.getCourse().getId(),
			enrollment.getStudent().getId(),
			enrollment.getStatus(),
			enrollment.getEnrolledAt(),
			enrollment.getCompletedAt(),
			enrollment.getCreatedAt(),
			enrollment.getUpdatedAt()
		);
	}
}

