package com.devnest.course.controller.teacher;

import com.devnest.course.dto.comment.CommentModerationRequest;
import com.devnest.course.dto.comment.CommentResponse;
import com.devnest.course.service.comment.CommentService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
@RequestMapping("/course/{courseId}/module/{moduleId}/lesson/{lessonId}/comment")
public class TeacherCommentController {

	private final CommentService commentService;

	@PostMapping("/{commentId}/moderation")
	public ResponseEntity<CommentResponse> moderate(
		@PathVariable UUID courseId,
		@PathVariable UUID moduleId,
		@PathVariable UUID lessonId,
		@PathVariable UUID commentId,
		@Valid @RequestBody CommentModerationRequest request
	) {
		var response = commentService.moderateByTeacher(
			courseId,
			moduleId,
			lessonId,
			commentId,
			request.moderationReason()
		);
		return ResponseEntity.ok(response);
	}
}
