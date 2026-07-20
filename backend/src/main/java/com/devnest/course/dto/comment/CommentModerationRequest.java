package com.devnest.course.dto.comment;

import jakarta.validation.constraints.NotBlank;

public record CommentModerationRequest(
	@NotBlank
	String moderationReason
) {
}

