package com.devnest.course.dto.question;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record QuestionUpdateRequest(
	@NotBlank
	String statement,
	@NotNull
	Integer position
) {
}

