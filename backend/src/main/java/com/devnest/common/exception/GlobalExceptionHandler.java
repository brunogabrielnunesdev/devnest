package com.devnest.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ConflictException.class)
	public ResponseEntity<ApiError> handleConflictException(
		ConflictException exception,
		HttpServletRequest request
	) {
		ApiError error = createError(HttpStatus.CONFLICT, exception.getMessage(), request);
		return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ApiError> handleBadCredentialsException(
		BadCredentialsException exception,
		HttpServletRequest request
	) {
		ApiError error = createError(HttpStatus.UNAUTHORIZED, exception.getMessage(), request);
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
	}

	@ExceptionHandler(UnauthorizedException.class)
	public ResponseEntity<ApiError> handleUnauthorizedException(
		UnauthorizedException exception,
		HttpServletRequest request
	) {
		ApiError error = createError(HttpStatus.UNAUTHORIZED, exception.getMessage(), request);
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> handleMethodArgumentNotValidException(
		MethodArgumentNotValidException exception,
		HttpServletRequest request
	) {
		Map<String, String> fieldErrors = new LinkedHashMap<>();
		exception.getBindingResult().getFieldErrors()
			.forEach(error -> fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage()));

		ApiError error = ApiError.of(
			HttpStatus.BAD_REQUEST.value(),
			HttpStatus.BAD_REQUEST.getReasonPhrase(),
			"Validation failed.",
			request.getRequestURI(),
			fieldErrors
		);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
	}

	@ExceptionHandler(ForbiddenException.class)
	public ResponseEntity<ApiError> handleForbiddenException(
		ForbiddenException exception,
		HttpServletRequest request
	) {
		ApiError error = createError(HttpStatus.FORBIDDEN, exception.getMessage(), request);
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiError> handleResourceNotFoundException(
		ResourceNotFoundException exception,
		HttpServletRequest request
	) {
		ApiError error = createError(HttpStatus.NOT_FOUND, exception.getMessage(), request);
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiError> handleUnexpectedException(
		Exception exception,
		HttpServletRequest request
	) {
		ApiError error = createError(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error.", request);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
	}

	private ApiError createError(HttpStatus status, String message, HttpServletRequest request) {
		return ApiError.of(
			status.value(),
			status.getReasonPhrase(),
			message,
			request.getRequestURI()
		);
	}
}
