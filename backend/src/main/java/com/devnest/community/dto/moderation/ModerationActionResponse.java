package com.devnest.community.dto.moderation;

import com.devnest.community.entity.moderation.ModerationActionType;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ModerationActionResponse(
		UUID id,
		UUID caseId,
		ModerationActionType action,
		UUID moderatorId,
		String reason,
		String previousState,
		String newState,
		OffsetDateTime performedAt
) {
}
