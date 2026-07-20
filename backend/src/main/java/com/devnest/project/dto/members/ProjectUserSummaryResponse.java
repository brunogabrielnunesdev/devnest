package com.devnest.project.dto.members;

import java.util.UUID;

public record ProjectUserSummaryResponse(
	UUID id,
	String email,
	String displayName
) {
}
