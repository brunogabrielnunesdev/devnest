package com.devnest.project.dto.project.updateproject;

import com.devnest.project.entity.project.ProjectStatus;
import com.devnest.project.entity.project.ProjectVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProjectUpdateRequest(
	@NotBlank
	@Size(max = 160)
	String title,

	String description,

	@NotNull
	ProjectStatus status,

	@NotNull
	ProjectVisibility visibility
) {
}

