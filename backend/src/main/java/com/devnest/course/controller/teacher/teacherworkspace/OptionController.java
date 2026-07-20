package com.devnest.course.controller.teacher.teacherworkspace;

import com.devnest.course.dto.option.OptionCreateRequest;
import com.devnest.course.dto.option.OptionResponse;
import com.devnest.course.dto.option.OptionUpdateRequest;
import com.devnest.course.mapper.option.OptionMapper;
import com.devnest.course.service.option.OptionService;
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
@PreAuthorize("hasRole('TEACHER')")
@RequestMapping("/course/{courseId}/module/{moduleId}/lesson/{lessonId}/quiz/question/{questionId}/option")
public class OptionController {

	private final OptionMapper optionMapper;
	private final OptionService optionService;

	@PostMapping
	public ResponseEntity<OptionResponse> create(
		@PathVariable UUID courseId,
		@PathVariable UUID moduleId,
		@PathVariable UUID lessonId,
		@PathVariable UUID questionId,
		@Valid @RequestBody OptionCreateRequest request
	) {
		var option = optionMapper.toEntity(request);
		var response = optionService.create(courseId, moduleId, lessonId, questionId, option);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping
	public ResponseEntity<List<OptionResponse>> findAll(
		@PathVariable UUID courseId,
		@PathVariable UUID moduleId,
		@PathVariable UUID lessonId,
		@PathVariable UUID questionId
	) {
		var response = optionService.findAll(courseId, moduleId, lessonId, questionId);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{optionId}")
	public ResponseEntity<OptionResponse> findById(
		@PathVariable UUID courseId,
		@PathVariable UUID moduleId,
		@PathVariable UUID lessonId,
		@PathVariable UUID questionId,
		@PathVariable UUID optionId
	) {
		var response = optionService.findById(courseId, moduleId, lessonId, questionId, optionId);
		return ResponseEntity.ok(response);
	}

	@PutMapping("/{optionId}")
	public ResponseEntity<OptionResponse> update(
		@PathVariable UUID courseId,
		@PathVariable UUID moduleId,
		@PathVariable UUID lessonId,
		@PathVariable UUID questionId,
		@PathVariable UUID optionId,
		@Valid @RequestBody OptionUpdateRequest request
	) {
		var option = optionMapper.toEntity(request);
		var response = optionService.update(courseId, moduleId, lessonId, questionId, optionId, option);
		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/{optionId}")
	public ResponseEntity<Void> delete(
		@PathVariable UUID courseId,
		@PathVariable UUID moduleId,
		@PathVariable UUID lessonId,
		@PathVariable UUID questionId,
		@PathVariable UUID optionId
	) {
		optionService.delete(courseId, moduleId, lessonId, questionId, optionId);
		return ResponseEntity.noContent().build();
	}
}
