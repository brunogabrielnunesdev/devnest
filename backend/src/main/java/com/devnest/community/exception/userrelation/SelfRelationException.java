package com.devnest.community.exception.userrelation;

public class SelfRelationException extends RuntimeException {

	public SelfRelationException() {
		super("Users cannot block or mute themselves.");
	}
}
