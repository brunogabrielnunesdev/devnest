package com.devnest.community.exception.reference;

public class ReferenceNotFoundException extends RuntimeException {

	public ReferenceNotFoundException(String resource) {
		super(resource + " referenced by the community post was not found.");
	}
}
