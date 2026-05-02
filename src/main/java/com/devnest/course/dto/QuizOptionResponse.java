package com.devnest.course.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record QuizOptionResponse(
	UUID id,
	UUID questionId,
	String text,
	Boolean correct,
	Integer position,
	OffsetDateTime createdAt,
	OffsetDateTime updatedAt
) {
}
