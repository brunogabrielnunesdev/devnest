package com.devnest.course.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CourseModuleResponse(
	UUID id,
	UUID courseId,
	String title,
	String description,
	Integer position,
	OffsetDateTime createdAt,
	OffsetDateTime updatedAt
) {
}
