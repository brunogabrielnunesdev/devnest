package com.devnest.community.exception.duplicate;

public class DuplicateContentException extends RuntimeException {

	public DuplicateContentException(String resource) {
		super("Duplicate " + resource + " content was recently submitted.");
	}
}
