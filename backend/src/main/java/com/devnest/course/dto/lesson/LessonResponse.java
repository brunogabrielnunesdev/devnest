package com.devnest.course.dto.lesson;

import java.time.OffsetDateTime;
import java.util.UUID;

public record LessonResponse(
	UUID id,
	UUID moduleId,
	String title,
	String description,
	String content,
	String videoUrl,
	Integer position,
	OffsetDateTime createdAt,
	OffsetDateTime updatedAt
) {
}

