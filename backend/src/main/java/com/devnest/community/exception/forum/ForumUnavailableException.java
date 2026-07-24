package com.devnest.community.exception.forum;

public class ForumUnavailableException extends RuntimeException {

	public ForumUnavailableException() {
		super("Community forum is not available for new posts.");
	}
}
