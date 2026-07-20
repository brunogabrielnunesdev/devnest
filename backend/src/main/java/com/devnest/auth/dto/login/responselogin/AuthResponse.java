package com.devnest.auth.dto.login.responselogin;


public record AuthResponse(
	String accessToken,
	String refreshToken
) {
}
