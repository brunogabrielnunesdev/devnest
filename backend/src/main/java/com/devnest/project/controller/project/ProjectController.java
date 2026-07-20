package com.devnest.project.controller.project;

import com.devnest.project.dto.project.CreateProjectRequest;
import com.devnest.project.dto.project.ProjectResponse;
import com.devnest.project.dto.project.updateproject.ProjectUpdateRequest;
import com.devnest.project.service.project.ProjectService;
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
@RequestMapping("/projects")
public class ProjectController {

	private final ProjectService projectService;

	@PostMapping
	public ResponseEntity<ProjectResponse> create(@Valid @RequestBody CreateProjectRequest request) {
		var response = projectService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping
	public ResponseEntity<List<ProjectResponse>> findAll() {
		var response = projectService.findAll();
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{projectId}")
	public ResponseEntity<ProjectResponse> findById(@PathVariable UUID projectId) {
		var response = projectService.findById(projectId);
		return ResponseEntity.ok(response);
	}

	@PatchMapping("/{projectId}")
	public ResponseEntity<ProjectResponse> update(
		@PathVariable UUID projectId,
		@Valid @RequestBody ProjectUpdateRequest request
	) {
		var response = projectService.update(projectId, request);
		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/{projectId}")
	public ResponseEntity<Void> delete(@PathVariable UUID projectId) {
		projectService.delete(projectId);
		return ResponseEntity.noContent().build();
	}
}

