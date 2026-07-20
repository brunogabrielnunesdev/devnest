package com.devnest.course.dto.course;

import com.devnest.course.entity.course.CourseStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CourseResponse(
	UUID id,
	UUID teacherId,
	String title,
	String description,
	String level,
	String coverImageUrl,
	CourseStatus status,
	OffsetDateTime createdAt,
	OffsetDateTime updatedAt
) {
}

