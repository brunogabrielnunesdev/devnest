package com.devnest.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record QuizQuestionUpdateRequest(
	@NotBlank
	String statement,
	@NotNull
	Integer position
) {
}
