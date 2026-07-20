package com.devnest.admin.dto.user;

import com.devnest.identity.entity.UserRole;
import com.devnest.identity.entity.UserStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record UserResponse(
	UUID id,
	String email,
	String displayName,
	String fullName,
	UserRole role,
	UserStatus status,
	OffsetDateTime createdAt,
	OffsetDateTime updatedAt
) {
}
