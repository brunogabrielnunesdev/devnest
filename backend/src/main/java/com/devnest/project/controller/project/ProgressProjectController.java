package com.devnest.project.controller.project;

import com.devnest.project.dto.project.updateproject.ProjectUpdateCreateRequest;
import com.devnest.project.dto.project.updateproject.ProjectUpdateResponse;
import com.devnest.project.dto.project.updateproject.ProjectUpdateUpdateRequest;
import com.devnest.project.mapper.ProjectUpdateMapper;
import com.devnest.project.service.project.ProjectUpdateService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@RequestMapping("/projects/{projectId}/updates")
public class ProgressProjectController {

	private final ProjectUpdateMapper projectUpdateMapper;
	private final ProjectUpdateService projectUpdateService;

	@PostMapping
	public ResponseEntity<ProjectUpdateResponse> create(
		@PathVariable UUID projectId,
		@Valid @RequestBody ProjectUpdateCreateRequest request
	) {
		var projectUpdate = projectUpdateMapper.toEntity(request);
		var response = projectUpdateService.create(projectId, projectUpdate);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping
	public ResponseEntity<List<ProjectUpdateResponse>> findAll(@PathVariable UUID projectId) {
		var response = projectUpdateService.findAll(projectId);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{updateId}")
	public ResponseEntity<ProjectUpdateResponse> findById(
		@PathVariable UUID projectId,
		@PathVariable UUID updateId
	) {
		var response = projectUpdateService.findById(projectId, updateId);
		return ResponseEntity.ok(response);
	}

	@PatchMapping("/{updateId}")
	public ResponseEntity<ProjectUpdateResponse> update(
		@PathVariable UUID projectId,
		@PathVariable UUID updateId,
		@Valid @RequestBody ProjectUpdateUpdateRequest request
	) {
		var projectUpdate = projectUpdateMapper.toEntity(request);
		var response = projectUpdateService.update(projectId, updateId, projectUpdate);
		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/{updateId}")
	public ResponseEntity<Void> delete(
		@PathVariable UUID projectId,
		@PathVariable UUID updateId
	) {
		projectUpdateService.delete(projectId, updateId);
		return ResponseEntity.noContent().build();
	}
}

