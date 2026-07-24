package com.devnest.community.controller.comment;

import com.devnest.community.dto.comment.CommentRequest;
import com.devnest.community.dto.comment.CommentResponse;
import com.devnest.community.service.comment.CommentService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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

@RestController("communityCommentController")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@RequestMapping("/community")
public class CommentController {

	private final CommentService commentService;

	@GetMapping("/posts/{postId}/comments")
	public ResponseEntity<Page<CommentResponse>> findByPost(
			@PathVariable UUID postId,
			@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable
	) {
		return ResponseEntity.ok(commentService.findByPost(postId, pageable));
	}

	@PostMapping("/posts/{postId}/comments")
	public ResponseEntity<CommentResponse> create(
			@PathVariable UUID postId,
			@Valid @RequestBody CommentRequest request
	) {
		return ResponseEntity.status(HttpStatus.CREATED).body(commentService.create(postId, request));
	}

	@PatchMapping("/comments/{commentId}")
	public ResponseEntity<CommentResponse> update(
			@PathVariable UUID commentId,
			@Valid @RequestBody CommentRequest request
	) {
		return ResponseEntity.ok(commentService.update(commentId, request));
	}

	@DeleteMapping("/comments/{commentId}")
	public ResponseEntity<Void> remove(@PathVariable UUID commentId) {
		commentService.remove(commentId, "Removed by author.");
		return ResponseEntity.noContent().build();
	}
}
