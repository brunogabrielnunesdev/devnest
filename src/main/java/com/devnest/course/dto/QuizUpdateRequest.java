package com.devnest.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record QuizUpdateRequest(
	@NotBlank
	@Size(max = 160)
	String title,
	@NotNull
	Integer passingScore,
	@NotNull
	Integer maxAttempts,
	@NotNull
	Integer maxQuestions
) {
}
