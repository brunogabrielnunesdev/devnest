package com.devnest.course.controller.student;

import com.devnest.course.dto.comment.CommentCreateRequest;
import com.devnest.course.dto.comment.CommentResponse;
import com.devnest.course.service.comment.CommentService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/course/{courseId}/lesson/{lessonId}/comment")
public class CommentController {

	private final CommentService commentService;

	@PostMapping
	@PreAuthorize("hasRole('STUDENT')")
	public ResponseEntity<CommentResponse> create(
		@PathVariable UUID courseId,
		@PathVariable UUID lessonId,
		@Valid @RequestBody CommentCreateRequest request
	) {
		var response = commentService.create(courseId, lessonId, request.content(), request.rating());
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<List<CommentResponse>> findVisibleComments(
		@PathVariable UUID courseId,
		@PathVariable UUID lessonId
	) {
		var response = commentService.findVisibleComments(courseId, lessonId);
		return ResponseEntity.ok(response);
	}
}
