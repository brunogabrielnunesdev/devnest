package com.devnest.community.exception.post;

public class PostUnavailableException extends RuntimeException {

	public PostUnavailableException() {
		super("Community post is not available for this operation.");
	}
}
