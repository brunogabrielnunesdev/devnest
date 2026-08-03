package com.devnest.community.dto.report;

import com.devnest.community.entity.report.ReportReason;
import com.devnest.community.entity.report.ReportStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ReportResponse(
		UUID id,
		UUID reporterId,
		UUID postId,
		UUID commentId,
		ReportReason reason,
		String description,
		ReportStatus status,
		UUID reviewedById,
		OffsetDateTime reviewedAt,
		String reviewNote,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt
) {
}
