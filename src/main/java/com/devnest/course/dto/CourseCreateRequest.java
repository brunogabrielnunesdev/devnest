package com.devnest.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CourseCreateRequest(
	@NotBlank
	@Size(max = 160)
	String title,

	String description,

	@Size(max = 40)
	String level
) {
}
