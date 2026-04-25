package com.devnest.shared.exception;

import java.time.LocalDateTime;

public record ApiError(
	int status,
	String error,
	String contextPath,
	LocalDateTime timestamp
) {
	public static ApiError of(int status, String error, String contextPath) {
		return new ApiError(status, error, contextPath, LocalDateTime.now());
	}
}
