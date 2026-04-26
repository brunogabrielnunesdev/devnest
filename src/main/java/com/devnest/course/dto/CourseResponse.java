package com.devnest.course.dto;

import com.devnest.course.domain.CourseStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CourseResponse(
	UUID id,
	UUID teacherId,
	String title,
	String description,
	String level,
	CourseStatus status,
	OffsetDateTime createdAt,
	OffsetDateTime updatedAt
) {
}
