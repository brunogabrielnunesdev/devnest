package com.devnest.course.controller.student;

import com.devnest.course.dto.course.enrollment.CourseEnrollmentResponse;
import com.devnest.course.dto.student.learning.StudentCourseLearningContentResponse;
import com.devnest.course.dto.course.progress.CourseProgressSummaryResponse;
import com.devnest.course.dto.lesson.LessonProgressResponse;
import com.devnest.course.service.course.CourseEnrollmentService;
import com.devnest.course.service.lesson.LessonProgressService;
import com.devnest.course.service.student.StudentLearningContentService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
@RequestMapping("/course")
public class StudentLearningController {

	private final CourseEnrollmentService courseEnrollmentService;
	private final LessonProgressService lessonProgressService;
	private final StudentLearningContentService studentLearningContentService;

	@PostMapping("/{courseId}/enrollment")
	public ResponseEntity<CourseEnrollmentResponse> enroll(@PathVariable UUID courseId) {
		var response = courseEnrollmentService.enroll(courseId);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/enrollment")
	public ResponseEntity<List<CourseEnrollmentResponse>> findMyEnrollments() {
		var response = courseEnrollmentService.findMyEnrollments();
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{courseId}/enrollment")
	public ResponseEntity<CourseEnrollmentResponse> findMyEnrollment(@PathVariable UUID courseId) {
		var response = courseEnrollmentService.findMyEnrollment(courseId);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/{courseId}/lesson/{lessonId}/progress")
	public ResponseEntity<LessonProgressResponse> completeLesson(
		@PathVariable UUID courseId,
		@PathVariable UUID lessonId
	) {
		var response = lessonProgressService.completeLesson(courseId, lessonId);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{courseId}/progress")
	public ResponseEntity<CourseProgressSummaryResponse> getCourseProgress(@PathVariable UUID courseId) {
		var response = lessonProgressService.getCourseProgress(courseId);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{courseId}/learning-content")
	public ResponseEntity<StudentCourseLearningContentResponse> getLearningContent(@PathVariable UUID courseId) {
		var response = studentLearningContentService.getLearningContent(courseId);
		return ResponseEntity.ok(response);
	}
}

