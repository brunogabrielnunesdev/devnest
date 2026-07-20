package com.devnest.project.dto.project;

import com.devnest.project.dto.members.ProjectUserSummaryResponse;
import com.devnest.project.entity.project.ProjectStatus;
import com.devnest.project.entity.project.ProjectVisibility;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ProjectResponse(
	UUID id,
	String title,
	String description,
	ProjectStatus status,
	ProjectVisibility visibility,
	ProjectUserSummaryResponse owner,
	double progress,
	long totalTasks,
	long completedTasks,
	OffsetDateTime createdAt,
	OffsetDateTime updatedAt
) {
}
