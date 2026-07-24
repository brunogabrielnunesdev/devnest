package com.devnest.community.exception;

import com.devnest.common.exception.ApiError;
import com.devnest.community.exception.access.CommunityForbiddenException;
import com.devnest.community.exception.comment.CommentNotFoundException;
import com.devnest.community.exception.comment.CommentUnavailableException;
import com.devnest.community.exception.duplicate.DuplicateContentException;
import com.devnest.community.exception.forum.ForumNotFoundException;
import com.devnest.community.exception.forum.ForumUnavailableException;
import com.devnest.community.exception.post.PostLimitExceededException;
import com.devnest.community.exception.post.PostNotFoundException;
import com.devnest.community.exception.post.PostUnavailableException;
import com.devnest.community.exception.ratelimit.CommunityRateLimitExceededException;
import com.devnest.community.exception.reference.ReferenceNotFoundException;
import com.devnest.community.exception.reaction.ReactionConflictException;
import com.devnest.community.exception.slug.SlugConflictException;
import com.devnest.community.exception.tag.TagNotFoundException;
import com.devnest.community.exception.userrelation.SelfRelationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class ExceptionHandler {

	@org.springframework.web.bind.annotation.ExceptionHandler({
			ForumNotFoundException.class,
			CommentNotFoundException.class,
			PostNotFoundException.class,
			TagNotFoundException.class,
			ReferenceNotFoundException.class
	})
	public ResponseEntity<ApiError> handleNotFound(
			RuntimeException exception,
			HttpServletRequest request
	) {
		return createResponse(HttpStatus.NOT_FOUND, exception.getMessage(), request);
	}

	@org.springframework.web.bind.annotation.ExceptionHandler({
			SlugConflictException.class,
			PostLimitExceededException.class,
			ForumUnavailableException.class,
			CommentUnavailableException.class,
			PostUnavailableException.class,
			ReactionConflictException.class,
			SelfRelationException.class,
			DuplicateContentException.class
	})
	public ResponseEntity<ApiError> handleConflict(
			RuntimeException exception,
			HttpServletRequest request
	) {
		return createResponse(HttpStatus.CONFLICT, exception.getMessage(), request);
	}

	@org.springframework.web.bind.annotation.ExceptionHandler(CommunityForbiddenException.class)
	public ResponseEntity<ApiError> handleForbidden(
			CommunityForbiddenException exception,
			HttpServletRequest request
	) {
		return createResponse(HttpStatus.FORBIDDEN, exception.getMessage(), request);
	}

	@org.springframework.web.bind.annotation.ExceptionHandler(CommunityRateLimitExceededException.class)
	public ResponseEntity<ApiError> handleRateLimit(
			CommunityRateLimitExceededException exception,
			HttpServletRequest request
	) {
		return createResponse(HttpStatus.TOO_MANY_REQUESTS, exception.getMessage(), request);
	}

	private ResponseEntity<ApiError> createResponse(
			HttpStatus status,
			String message,
			HttpServletRequest request
	) {
		ApiError error = ApiError.of(
				status.value(),
				status.getReasonPhrase(),
				message,
				request.getRequestURI()
		);
		return ResponseEntity.status(status).body(error);
	}
}
