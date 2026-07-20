package com.devnest.course.controller.teacher.teacherworkspace;

import com.devnest.course.dto.course.CourseCreateRequest;
import com.devnest.course.dto.course.CourseResponse;
import com.devnest.course.dto.course.update.CourseUpdateRequest;
import com.devnest.course.mapper.course.CourseMapper;
import com.devnest.course.service.course.CourseService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/course")
public class CourseController {

	private final CourseMapper courseMapper;
	private final CourseService courseService;

	@PostMapping
	@PreAuthorize("hasRole('TEACHER')")
	public ResponseEntity<CourseResponse> create(@Valid @RequestBody CourseCreateRequest request) {
		var course = courseMapper.toEntity(request);
		var response = courseService.create(course);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/my")
	@PreAuthorize("hasRole('TEACHER')")
	public ResponseEntity<List<CourseResponse>> findAll() {
		var response = courseService.findAll();
		return ResponseEntity.ok(response);
	}

	@GetMapping("/catalog")
	public ResponseEntity<List<CourseResponse>> findPublishedCourses() {
		var response = courseService.findPublishedCourses();
		return ResponseEntity.ok(response);
	}

	@GetMapping("/catalog/{courseId}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<CourseResponse> findPublishedCourseById(@PathVariable UUID courseId) {
		var response = courseService.findPublishedCourseById(courseId);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{courseId}")
	@PreAuthorize("hasRole('TEACHER')")
	public ResponseEntity<CourseResponse> findById(@PathVariable UUID courseId) {
		var response = courseService.findById(courseId);
		return ResponseEntity.ok(response);
	}

	@PutMapping("/{courseId}")
	@PreAuthorize("hasRole('TEACHER')")
	public ResponseEntity<CourseResponse> update(
		@PathVariable UUID courseId,
		@Valid @RequestBody CourseUpdateRequest request
	) {
		var course = courseMapper.toEntity(request);
		var response = courseService.update(courseId, course);

		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/{courseId}")
	@PreAuthorize("hasRole('TEACHER')")
	public ResponseEntity<Void> delete(@PathVariable UUID courseId) {
		courseService.delete(courseId);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{courseId}/publish")
	@PreAuthorize("hasRole('TEACHER')")
	public ResponseEntity<CourseResponse> publish(@PathVariable UUID courseId) {
		var response = courseService.publish(courseId);
		return ResponseEntity.ok(response);
	}
}

