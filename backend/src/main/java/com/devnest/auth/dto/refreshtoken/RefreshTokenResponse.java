package com.devnest.auth.dto.refreshtoken;

public record RefreshTokenResponse(
	String accessToken,
	String refreshToken
) {
}
