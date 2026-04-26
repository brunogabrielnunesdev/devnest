package com.devnest.shared.exception;

import jakarta.servlet.http.HttpServletRequest;
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
		ApiError error = createError(HttpStatus.CONFLICT, request);
		return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ApiError> handleBadCredentialsException(
		BadCredentialsException exception,
		HttpServletRequest request
	) {
		ApiError error = createError(HttpStatus.UNAUTHORIZED, request);
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
	}

	@ExceptionHandler(UnauthorizedException.class)
	public ResponseEntity<ApiError> handleUnauthorizedException(
		UnauthorizedException exception,
		HttpServletRequest request
	) {
		ApiError error = createError(HttpStatus.UNAUTHORIZED, request);
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> handleMethodArgumentNotValidException(
		MethodArgumentNotValidException exception,
		HttpServletRequest request
	) {
		ApiError error = createError(HttpStatus.BAD_REQUEST, request);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
	}

	@ExceptionHandler(ForbiddenException.class)
	public ResponseEntity<ApiError> handleForbiddenException(
		ForbiddenException exception,
		HttpServletRequest request
	) {
		ApiError error = createError(HttpStatus.FORBIDDEN, request);
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiError> handleResourceNotFoundException(
		ResourceNotFoundException exception,
		HttpServletRequest request
	) {
		ApiError error = createError(HttpStatus.NOT_FOUND, request);
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
	}

	private ApiError createError(HttpStatus status, HttpServletRequest request) {
		return ApiError.of(
			status.value(),
			status.getReasonPhrase(),
			request.getRequestURI()
		);
	}
}
