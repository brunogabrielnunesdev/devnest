package com.devnest.auth.dto;

import com.devnest.user.domain.UserRole;
import java.util.UUID;

public record AuthResponse(
	String accessToken,
	String tokenType,
	UUID userId,
	String email,
	UserRole role
) {
	public static AuthResponse bearer(String accessToken, UUID userId, String email, UserRole role) {
		return new AuthResponse(accessToken, "Bearer", userId, email, role);
	}
}
