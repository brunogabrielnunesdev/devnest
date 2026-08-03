package com.devnest.community.dto.moderation;

import com.devnest.community.entity.moderation.ModerationCaseStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ModerationCaseResponse(
		UUID id,
		UUID reportId,
		UUID postId,
		UUID commentId,
		ModerationCaseStatus status,
		UUID openedById,
		OffsetDateTime openedAt,
		UUID resolvedById,
		OffsetDateTime resolvedAt
) {
}
