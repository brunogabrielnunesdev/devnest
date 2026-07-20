package com.devnest.course.dto.quiz;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record QuizCreateRequest(
	@NotBlank
	@Size(max = 160)
	String title,
	@NotNull
	@Min(0)
	@Max(100)
	Integer passingScore,
	@NotNull
	@Min(1)
	@Max(10)
	Integer maxAttempts,
	@NotNull
	@Min(1)
	@Max(50)
	Integer maxQuestions
) {
}

