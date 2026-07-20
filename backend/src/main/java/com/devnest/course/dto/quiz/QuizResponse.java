package com.devnest.course.dto.quiz;

import java.time.OffsetDateTime;
import java.util.UUID;

public record QuizResponse(
	UUID id,
	UUID lessonId,
	String title,
	Integer passingScore,
	Integer maxAttempts,
	Integer maxQuestions,
	OffsetDateTime createdAt,
	OffsetDateTime updatedAt
) {
}

