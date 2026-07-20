package com.devnest.common.exception;

import java.time.OffsetDateTime;
import java.util.Map;

public record ApiError(
	int status,
	String error,
	String message,
	String path,
	Map<String, String> fieldErrors,
	OffsetDateTime timestamp
) {
	public static ApiError of(int status, String error, String message, String path) {
		return new ApiError(status, error, message, path, Map.of(), OffsetDateTime.now());
	}

	public static ApiError of(
		int status,
		String error,
		String message,
		String path,
		Map<String, String> fieldErrors
	) {
		return new ApiError(status, error, message, path, fieldErrors, OffsetDateTime.now());
	}
}
