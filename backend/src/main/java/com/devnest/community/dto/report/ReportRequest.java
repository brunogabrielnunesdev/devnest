package com.devnest.community.dto.report;

import com.devnest.community.entity.report.ReportReason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReportRequest(
		@NotNull ReportReason reason,
		@Size(max = 1000) String description
) {
}
