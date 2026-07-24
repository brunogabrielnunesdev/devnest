package com.devnest.community.exception.slug;

public class SlugConflictException extends RuntimeException {

	public SlugConflictException() {
		super("Community forum slug is already in use.");
	}
}
