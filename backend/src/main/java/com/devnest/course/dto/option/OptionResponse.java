package com.devnest.course.dto.option;

import java.time.OffsetDateTime;
import java.util.UUID;

public record OptionResponse(
	UUID id,
	UUID questionId,
	String text,
	Boolean correct,
	Integer position,
	OffsetDateTime createdAt,
	OffsetDateTime updatedAt
) {
}

