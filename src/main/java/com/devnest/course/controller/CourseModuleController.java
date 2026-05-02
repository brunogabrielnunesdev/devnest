package com.devnest.course.controller;

import com.devnest.course.dto.CourseModuleCreateRequest;
import com.devnest.course.dto.CourseModuleResponse;
import com.devnest.course.dto.CourseModuleUpdateRequest;
import com.devnest.course.mapper.CourseModuleMapper;
import com.devnest.course.service.CourseModuleService;
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
@RequestMapping("/courses/{courseId}/modules")
public class CourseModuleController {

	private final CourseModuleMapper courseModuleMapper;
	private final CourseModuleService courseModuleService;

	@PostMapping
	public ResponseEntity<CourseModuleResponse> create(
		@PathVariable UUID courseId,
		@Valid @RequestBody CourseModuleCreateRequest request
	) {
		var module = courseModuleMapper.toEntity(request);
		var response = courseModuleService.create(courseId, module);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping
	public ResponseEntity<List<CourseModuleResponse>> findAll(@PathVariable UUID courseId) {
		var response = courseModuleService.findAll(courseId);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{moduleId}")
	public ResponseEntity<CourseModuleResponse> findById(@PathVariable UUID courseId, @PathVariable UUID moduleId) {
		var response = courseModuleService.findById(courseId, moduleId);
		return ResponseEntity.ok(response);
	}

	@PutMapping("/{moduleId}")
	public ResponseEntity<CourseModuleResponse> update(
		@PathVariable UUID courseId,
		@PathVariable UUID moduleId,
		@Valid @RequestBody CourseModuleUpdateRequest request
	) {
		var module = courseModuleMapper.toEntity(request);
		var response = courseModuleService.update(courseId, moduleId, module);
		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/{moduleId}")
	public ResponseEntity<Void> delete(@PathVariable UUID courseId, @PathVariable UUID moduleId) {
		courseModuleService.delete(courseId, moduleId);
		return ResponseEntity.noContent().build();
	}
}
