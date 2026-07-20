package com.devnest.course.dto.module;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ModuleResponse(
	UUID id,
	UUID courseId,
	String title,
	String description,
	Integer position,
	OffsetDateTime createdAt,
	OffsetDateTime updatedAt
) {
}

