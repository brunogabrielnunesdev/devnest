package com.devnest.community.exception.report;

public class ReportNotFoundException extends RuntimeException {

	public ReportNotFoundException() {
		super("Report not found.");
	}
}
