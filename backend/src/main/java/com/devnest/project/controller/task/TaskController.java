package com.devnest.project.controller.task;

import com.devnest.project.dto.task.TaskCreateRequest;
import com.devnest.project.dto.task.TaskResponse;
import com.devnest.project.dto.task.TaskUpdateRequest;
import com.devnest.project.service.task.TaskService;
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
@RequestMapping("/projects/{projectId}/tasks")
public class TaskController {

	private final TaskService taskService;

	@PostMapping
	public ResponseEntity<TaskResponse> create(
		@PathVariable UUID projectId,
		@Valid @RequestBody TaskCreateRequest request
	) {
		return ResponseEntity.status(HttpStatus.CREATED).body(taskService.create(projectId, request));
	}

	@GetMapping
	public ResponseEntity<List<TaskResponse>> findAll(@PathVariable UUID projectId) {
		return ResponseEntity.ok(taskService.findAll(projectId));
	}

	@PatchMapping("/{taskId}")
	public ResponseEntity<TaskResponse> update(
		@PathVariable UUID projectId,
		@PathVariable UUID taskId,
		@Valid @RequestBody TaskUpdateRequest request
	) {
		return ResponseEntity.ok(taskService.update(projectId, taskId, request));
	}

	@DeleteMapping("/{taskId}")
	public ResponseEntity<Void> delete(@PathVariable UUID projectId, @PathVariable UUID taskId) {
		taskService.delete(projectId, taskId);
		return ResponseEntity.noContent().build();
	}
}
