package com.devnest.admin.controller.lessoncomment;

import com.devnest.admin.dto.comment.retainedcomment.RetainedCommentResponse;
import com.devnest.admin.service.lessoncomment.LessonCommentService;
import com.devnest.course.dto.comment.CommentModerationRequest;
import com.devnest.course.dto.comment.CommentResponse;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("adminLessonCommentController")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/admin/comment/lesson")
public class LessonCommentController {

	private final LessonCommentService lessonCommentService;

	@GetMapping("/retained")
	public ResponseEntity<List<RetainedCommentResponse>> findRetainedComments() {
		var response = lessonCommentService.findRetainedComments();
		return ResponseEntity.ok(response);
	}

	@PostMapping("/{commentId}/moderation")
	public ResponseEntity<CommentResponse> moderate(
		@PathVariable UUID commentId,
		@Valid @RequestBody CommentModerationRequest request
	) {
		var response = lessonCommentService.moderate(commentId, request.moderationReason());
		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/{commentId}")
	public ResponseEntity<Void> deleteRetainedComment(@PathVariable UUID commentId) {
		lessonCommentService.deleteRetainedComment(commentId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
}

