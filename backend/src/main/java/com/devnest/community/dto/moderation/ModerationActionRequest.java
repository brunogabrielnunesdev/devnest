package com.devnest.community.dto.moderation;

import com.devnest.community.entity.moderation.ModerationActionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ModerationActionRequest(
		@NotNull ModerationActionType action,
		@NotBlank @Size(max = 1000) String reason
) {
}
