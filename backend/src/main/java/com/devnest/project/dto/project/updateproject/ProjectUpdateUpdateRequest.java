package com.devnest.project.dto.project.updateproject;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProjectUpdateUpdateRequest(
	@NotBlank
	@Size(max = 160)
	String title,

	@NotBlank
	String content
) {
}

