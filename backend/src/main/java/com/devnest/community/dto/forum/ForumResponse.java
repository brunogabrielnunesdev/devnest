package com.devnest.community.dto.forum;

import com.devnest.community.entity.forum.ForumStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ForumResponse(
		UUID id,
		UUID createdById,
		String name,
		String slug,
		String description,
		ForumStatus status,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt
) {
}
