package com.devnest.community.dto.userrelation;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserRelationResponse(
		UUID userId,
		String displayName,
		OffsetDateTime createdAt
) {
}
