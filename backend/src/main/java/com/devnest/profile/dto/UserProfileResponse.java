package com.devnest.profile.dto;

import com.devnest.identity.entity.UserRole;
import com.devnest.identity.entity.UserStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record UserProfileResponse(
	UUID userId,
	String email,
	UserRole role,
	UserStatus status,
	String displayName,
	String fullName,
	String bio,
	String avatarUrl,
	String githubUrl,
	String linkedinUrl,
	String portfolioUrl,
	String location,
	OffsetDateTime createdAt,
	OffsetDateTime updatedAt
) {
}
