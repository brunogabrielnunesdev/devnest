package com.devnest.community.dto.report;

import com.devnest.community.entity.report.ReportDecision;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReportReviewRequest(
		@NotNull ReportDecision decision,
		@NotBlank @Size(max = 1000) String note
) {
}
