package com.devnest.community.dto.reaction;

import com.devnest.community.entity.reaction.ReactionType;
import jakarta.validation.constraints.NotNull;

public record ReactionRequest(@NotNull ReactionType type) {
}
