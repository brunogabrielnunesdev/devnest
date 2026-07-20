package com.devnest.project.controller.note;

import com.devnest.project.dto.note.NoteCreateRequest;
import com.devnest.project.dto.note.NoteResponse;
import com.devnest.project.dto.note.NoteUpdateRequest;
import com.devnest.project.service.note.NoteService;
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
@RequestMapping("/projects/{projectId}/notes")
public class NoteController {

	private final NoteService noteService;

	@PostMapping
	public ResponseEntity<NoteResponse> create(
		@PathVariable UUID projectId,
		@Valid @RequestBody NoteCreateRequest request
	) {
		return ResponseEntity.status(HttpStatus.CREATED).body(noteService.create(projectId, request));
	}

	@GetMapping
	public ResponseEntity<List<NoteResponse>> findAll(@PathVariable UUID projectId) {
		return ResponseEntity.ok(noteService.findAll(projectId));
	}

	@PatchMapping("/{noteId}")
	public ResponseEntity<NoteResponse> update(
		@PathVariable UUID projectId,
		@PathVariable UUID noteId,
		@Valid @RequestBody NoteUpdateRequest request
	) {
		return ResponseEntity.ok(noteService.update(projectId, noteId, request));
	}

	@DeleteMapping("/{noteId}")
	public ResponseEntity<Void> delete(@PathVariable UUID projectId, @PathVariable UUID noteId) {
		noteService.delete(projectId, noteId);
		return ResponseEntity.noContent().build();
	}
}
