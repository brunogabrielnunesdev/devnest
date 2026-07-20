package com.devnest.course.dto.module;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ModuleCreateRequest(
	@NotBlank
	@Size(max = 160)
	String title,
	String description,
	@NotNull
	Integer position
) {
}

