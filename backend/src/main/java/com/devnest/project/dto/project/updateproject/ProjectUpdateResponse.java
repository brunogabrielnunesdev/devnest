package com.devnest.project.dto.project.updateproject;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ProjectUpdateResponse(
	UUID id,
	UUID projectId,
	String title,
	String content,
	OffsetDateTime createdAt,
	OffsetDateTime updatedAt
) {
}

