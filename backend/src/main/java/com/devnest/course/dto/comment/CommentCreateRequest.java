package com.devnest.course.dto.comment;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CommentCreateRequest(
	@NotBlank
	String content,

	@Min(1)
	@Max(10)
	Integer rating
) {
}

