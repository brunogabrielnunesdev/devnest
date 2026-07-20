package com.devnest.admin.controller.comment;

import com.devnest.admin.dto.comment.CommentResponse;
import com.devnest.admin.dto.adminpage.AdminPageResponse;
import com.devnest.admin.service.comment.CommentService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController("adminCommentController")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/admin/comments")
public class CommentController {

	private final CommentService commentService;

	@GetMapping
	public ResponseEntity<AdminPageResponse<CommentResponse>> findAll(
		@RequestParam(required = false) String query,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size
	) {
		return ResponseEntity.ok(commentService.findAll(query, page, size));
	}

	@GetMapping("/all")
	public ResponseEntity<List<CommentResponse>> findAllList(
		@RequestParam(required = false) String query
	) {
		return ResponseEntity.ok(commentService.findAllList(query));
	}

	@PatchMapping("/{id}/hide")
	public ResponseEntity<CommentResponse> hide(@PathVariable UUID id) {
		return ResponseEntity.ok(commentService.hide(id));
	}

	@PatchMapping("/{id}/restore")
	public ResponseEntity<CommentResponse> restore(@PathVariable UUID id) {
		return ResponseEntity.ok(commentService.restore(id));
	}
}
