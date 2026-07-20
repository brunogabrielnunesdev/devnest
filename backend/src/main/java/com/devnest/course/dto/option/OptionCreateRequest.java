package com.devnest.course.dto.option;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OptionCreateRequest(
	@NotBlank
	String text,
	@NotNull
	Boolean correct,
	@NotNull
	Integer position
) {
}

