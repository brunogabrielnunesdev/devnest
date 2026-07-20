package com.devnest.project.dto.members;

import com.devnest.project.entity.member.MemberRole;
import java.time.OffsetDateTime;
import java.util.UUID;

public record MemberResponse(
	UUID id,
	UUID projectId,
	ProjectUserSummaryResponse user,
	MemberRole role,
	OffsetDateTime createdAt
) {
}
