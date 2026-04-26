package com.devnest.course;

import com.devnest.course.dto.CourseCreateRequest;
import com.devnest.course.dto.CourseResponse;
import com.devnest.course.dto.CourseUpdateRequest;
import com.devnest.course.mapper.CourseMapper;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/courses")
public class CourseController {

	private final CourseMapper courseMapper;
	private final CourseService courseService;

	@PostMapping
	public ResponseEntity<CourseResponse> create(@Valid @RequestBody CourseCreateRequest request) {
		var course = courseMapper.toEntity(request);
		var response = courseService.create(course);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/my")
	public ResponseEntity<List<CourseResponse>> findAll() {
		var response = courseService.findAll();
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{courseId}")
	public ResponseEntity<CourseResponse> findById(@PathVariable UUID courseId) {
		var response = courseService.findById(courseId);
		return ResponseEntity.ok(response);
	}

	@PutMapping("/{courseId}")
	public ResponseEntity<CourseResponse> update(
		@PathVariable UUID courseId,
		@Valid @RequestBody CourseUpdateRequest request
	) {
		var course = courseMapper.toEntity(request);
		var response = courseService.update(courseId, course);

		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/{courseId}")
	public ResponseEntity<Void> delete(@PathVariable UUID courseId) {
		courseService.delete(courseId);

		return ResponseEntity.noContent().build();
	}
}
