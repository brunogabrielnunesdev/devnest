package com.devnest.course.dto.lesson.update;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LessonUpdateRequest(
	@NotBlank
	@Size(max = 160)
	String title,
	String description,
	String content,
	@Pattern(regexp = "^$|https?://.+", message = "Video URL must be a valid http or https URL.")
	String videoUrl,
	@NotNull
	Integer position
) {
}

