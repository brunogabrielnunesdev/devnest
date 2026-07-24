package com.devnest.community.dto.reaction;

import com.devnest.community.entity.reaction.ReactionType;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ReactionResponse(
		UUID id,
		UUID userId,
		UUID postId,
		UUID commentId,
		ReactionType type,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt
) {
}
