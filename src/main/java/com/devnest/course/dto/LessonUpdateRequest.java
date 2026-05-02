package com.devnest.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LessonUpdateRequest(
	@NotBlank
	@Size(max = 160)
	String title,
	String description,
	String content,
	String videoUrl,
	@NotNull
	Integer position
) {
}
