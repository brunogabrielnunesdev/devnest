package com.devnest.project.dto.note;

import com.devnest.project.dto.members.ProjectUserSummaryResponse;

import java.time.OffsetDateTime;
import java.util.UUID;

public record NoteResponse(
	UUID id,
	UUID projectId,
	ProjectUserSummaryResponse author,
	String content,
	OffsetDateTime createdAt,
	OffsetDateTime updatedAt
) {
}
