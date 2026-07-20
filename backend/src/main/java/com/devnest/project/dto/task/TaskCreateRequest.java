package com.devnest.project.dto.task;

import com.devnest.project.entity.task.TaskPriority;
import com.devnest.project.entity.task.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

public record TaskCreateRequest(
	@NotBlank
	@Size(max = 160)
	String title,

	String description,

	@NotNull
	TaskStatus status,

	@NotNull
    TaskPriority priority,

	UUID assignedToId,
	LocalDate dueDate
) {
}
