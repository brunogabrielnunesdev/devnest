package com.devnest.community.exception.ratelimit;

public class CommunityRateLimitExceededException extends RuntimeException {

	public CommunityRateLimitExceededException(String resource, int limit) {
		super("Rate limit exceeded for " + resource + ": maximum " + limit + " per minute.");
	}
}
