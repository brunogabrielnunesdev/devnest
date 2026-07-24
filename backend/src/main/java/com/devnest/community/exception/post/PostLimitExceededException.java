package com.devnest.community.exception.post;

public class PostLimitExceededException extends RuntimeException {

	public PostLimitExceededException() {
		super("Community post limit for the current time window has been reached.");
	}
}
