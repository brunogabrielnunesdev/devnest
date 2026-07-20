package com.devnest.course.dto.question;

import java.time.OffsetDateTime;
import java.util.UUID;

public record QuestionResponse(
	UUID id,
	UUID quizId,
	String statement,
	Integer position,
	OffsetDateTime createdAt,
	OffsetDateTime updatedAt
) {
}

