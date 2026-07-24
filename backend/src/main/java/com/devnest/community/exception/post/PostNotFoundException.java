package com.devnest.community.exception.post;

public class PostNotFoundException extends RuntimeException {

	public PostNotFoundException() {
		super("Community post not found.");
	}
}
