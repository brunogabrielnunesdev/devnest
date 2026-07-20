package com.devnest.project.dto.project;

import com.devnest.project.entity.project.ProjectVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateProjectRequest(
	@NotBlank
	@Size(max = 160)
	String title,

	String description,

	@NotNull
	ProjectVisibility visibility
) {
}

