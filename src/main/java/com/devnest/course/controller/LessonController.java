package com.devnest.course.controller;

import com.devnest.course.dto.LessonCreateRequest;
import com.devnest.course.dto.LessonResponse;
import com.devnest.course.dto.LessonUpdateRequest;
import com.devnest.course.mapper.LessonMapper;
import com.devnest.course.service.LessonService;
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
@RequestMapping("/courses/{courseId}/modules/{moduleId}/lessons")
public class LessonController {

	private final LessonMapper lessonMapper;
	private final LessonService lessonService;

	@PostMapping
	public ResponseEntity<LessonResponse> create(
		@PathVariable UUID courseId,
		@PathVariable UUID moduleId,
		@Valid @RequestBody LessonCreateRequest request
	) {
		var lesson = lessonMapper.toEntity(request);
		var response = lessonService.create(courseId, moduleId, lesson);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping
	public ResponseEntity<List<LessonResponse>> findAll(@PathVariable UUID courseId, @PathVariable UUID moduleId) {
		var response = lessonService.findAll(courseId, moduleId);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{lessonId}")
	public ResponseEntity<LessonResponse> findById(
		@PathVariable UUID courseId,
		@PathVariable UUID moduleId,
		@PathVariable UUID lessonId
	) {
		var response = lessonService.findById(courseId, moduleId, lessonId);
		return ResponseEntity.ok(response);
	}

	@PutMapping("/{lessonId}")
	public ResponseEntity<LessonResponse> update(
		@PathVariable UUID courseId,
		@PathVariable UUID moduleId,
		@PathVariable UUID lessonId,
		@Valid @RequestBody LessonUpdateRequest request
	) {
		var lesson = lessonMapper.toEntity(request);
		var response = lessonService.update(courseId, moduleId, lessonId, lesson);
		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/{lessonId}")
	public ResponseEntity<Void> delete(
		@PathVariable UUID courseId,
		@PathVariable UUID moduleId,
		@PathVariable UUID lessonId
	) {
		lessonService.delete(courseId, moduleId, lessonId);
		return ResponseEntity.noContent().build();
	}
}
