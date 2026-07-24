package com.devnest.community.dto.forum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ForumRequest(
		@NotBlank
		@Size(max = 80)
		String name,

		@NotBlank
		@Size(max = 100)
		@Pattern(
				regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$",
				message = "Slug must contain only lowercase letters, numbers and hyphens."
		)
		String slug,

		@NotBlank
		@Size(max = 500)
		String description
) {
}
