package com.devnest.admin.dto.course;

import com.devnest.course.entity.course.CourseStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CourseResponse(
	UUID id,
	UUID teacherId,
	String teacherName,
	String title,
	String description,
	String level,
	String coverImageUrl,
	CourseStatus status,
	boolean archived,
	OffsetDateTime createdAt,
	OffsetDateTime updatedAt
) {
}
