package com.devnest.project.dto.task;

import com.devnest.project.dto.members.ProjectUserSummaryResponse;
import com.devnest.project.entity.task.TaskPriority;
import com.devnest.project.entity.task.TaskStatus;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TaskResponse(
	UUID id,
	UUID projectId,
	String title,
	String description,
	TaskStatus status,
	TaskPriority priority,
	ProjectUserSummaryResponse assignedTo,
	LocalDate dueDate,
	OffsetDateTime createdAt,
	OffsetDateTime updatedAt
) {
}
