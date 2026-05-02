package com.devnest.course.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record QuizQuestionResponse(
	UUID id,
	UUID quizId,
	String statement,
	Integer position,
	OffsetDateTime createdAt,
	OffsetDateTime updatedAt
) {
}
