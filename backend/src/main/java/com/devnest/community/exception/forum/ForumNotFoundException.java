package com.devnest.community.exception.forum;

public class ForumNotFoundException extends RuntimeException {

	public ForumNotFoundException() {
		super("Community forum not found.");
	}
}
