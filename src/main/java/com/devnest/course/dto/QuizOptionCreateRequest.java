package com.devnest.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record QuizOptionCreateRequest(
	@NotBlank
	String text,
	@NotNull
	Boolean correct,
	@NotNull
	Integer position
) {
}
