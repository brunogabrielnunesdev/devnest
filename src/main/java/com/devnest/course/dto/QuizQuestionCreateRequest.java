package com.devnest.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record QuizQuestionCreateRequest(
	@NotBlank
	String statement,
	@NotNull
	Integer position
) {
}
