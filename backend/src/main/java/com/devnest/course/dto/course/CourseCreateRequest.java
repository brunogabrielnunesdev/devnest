package com.devnest.course.dto.course;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CourseCreateRequest(
	@NotBlank
	@Size(max = 160)
	String title,

	String description,

	@Size(max = 40)
	String level,

	@Size(max = 500)
	@Pattern(regexp = "^$|https?://.+", message = "Cover image URL must be a valid http or https URL.")
	String coverImageUrl
) {
	public CourseCreateRequest(String title, String description, String level) {
		this(title, description, level, null);
	}
}

