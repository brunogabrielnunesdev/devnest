package com.devnest.project.dto.members;

import com.devnest.project.entity.member.MemberRole;
import jakarta.validation.constraints.NotNull;

public record MemberUpdateRequest(
	@NotNull
    MemberRole role
) {
}
