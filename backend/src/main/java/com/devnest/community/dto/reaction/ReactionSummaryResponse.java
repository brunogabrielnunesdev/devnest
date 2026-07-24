package com.devnest.community.dto.reaction;

import com.devnest.community.entity.reaction.ReactionType;
import java.util.Map;

public record ReactionSummaryResponse(
		Map<ReactionType, Long> counts,
		long total,
		ReactionType currentUserReaction
) {
}
