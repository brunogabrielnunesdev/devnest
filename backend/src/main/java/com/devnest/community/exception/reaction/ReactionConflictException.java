package com.devnest.community.exception.reaction;

public class ReactionConflictException extends RuntimeException {

	public ReactionConflictException() {
		super("The reaction changed concurrently. Retry the request.");
	}
}
