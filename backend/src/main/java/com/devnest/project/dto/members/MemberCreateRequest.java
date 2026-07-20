package com.devnest.project.dto.members;

import com.devnest.project.entity.member.MemberRole;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record MemberCreateRequest(
	@NotNull
	UUID userId,

	@NotNull
    MemberRole role
) {
}
