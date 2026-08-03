package com.devnest.community.exception.moderation;

public class ModerationCaseNotFoundException extends RuntimeException {
	public ModerationCaseNotFoundException() {
		super("Moderation case not found.");
	}
}
