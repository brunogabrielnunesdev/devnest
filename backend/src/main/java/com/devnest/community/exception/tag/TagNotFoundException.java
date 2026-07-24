package com.devnest.community.exception.tag;

public class TagNotFoundException extends RuntimeException {

	public TagNotFoundException() {
		super("One or more community tags were not found.");
	}
}
