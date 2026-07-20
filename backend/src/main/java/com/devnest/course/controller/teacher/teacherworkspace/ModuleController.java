package com.devnest.course.controller.teacher.teacherworkspace;

import com.devnest.course.dto.module.ModuleCreateRequest;
import com.devnest.course.dto.module.ModuleResponse;
import com.devnest.course.dto.module.ModuleUpdateRequest;
import com.devnest.course.mapper.module.ModuleMapper;
import com.devnest.course.service.module.ModuleService;
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
@RequestMapping("/course/{courseId}/module")
public class ModuleController {

	private final ModuleMapper moduleMapper;
	private final ModuleService moduleService;

	@PostMapping
	public ResponseEntity<ModuleResponse> create(
		@PathVariable UUID courseId,
		@Valid @RequestBody ModuleCreateRequest request
	) {
		var module = moduleMapper.toEntity(request);
		var response = moduleService.create(courseId, module);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping
	public ResponseEntity<List<ModuleResponse>> findAll(@PathVariable UUID courseId) {
		var response = moduleService.findAll(courseId);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{moduleId}")
	public ResponseEntity<ModuleResponse> findById(@PathVariable UUID courseId, @PathVariable UUID moduleId) {
		var response = moduleService.findById(courseId, moduleId);
		return ResponseEntity.ok(response);
	}

	@PutMapping("/{moduleId}")
	public ResponseEntity<ModuleResponse> update(
		@PathVariable UUID courseId,
		@PathVariable UUID moduleId,
		@Valid @RequestBody ModuleUpdateRequest request
	) {
		var module = moduleMapper.toEntity(request);
		var response = moduleService.update(courseId, moduleId, module);
		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/{moduleId}")
	public ResponseEntity<Void> delete(@PathVariable UUID courseId, @PathVariable UUID moduleId) {
		moduleService.delete(courseId, moduleId);
		return ResponseEntity.noContent().build();
	}
}
