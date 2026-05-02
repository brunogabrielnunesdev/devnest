package com.devnest.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CourseModuleUpdateRequest(
	@NotBlank
	@Size(max = 160)
	String title,
	String description,
	@NotNull
	Integer position
) {
}
