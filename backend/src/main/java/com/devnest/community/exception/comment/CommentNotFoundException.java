package com.devnest.community.exception.comment;

public class CommentNotFoundException extends RuntimeException {

	public CommentNotFoundException() {
		super("Community comment not found.");
	}
}
