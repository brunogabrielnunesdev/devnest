package com.devnest.course.dto.lesson;

import java.time.OffsetDateTime;
import java.util.UUID;

public record LessonProgressResponse(
	UUID id,
	UUID lessonId,
	UUID studentId,
	Boolean completed,
	OffsetDateTime completedAt,
	OffsetDateTime createdAt,
	OffsetDateTime updatedAt
) {
}

