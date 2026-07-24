package com.devnest.community.dto.post;

import com.devnest.community.dto.forum.ForumResponse;
import com.devnest.community.dto.tag.TagResponse;
import com.devnest.community.entity.post.ContentStatus;
import com.devnest.community.entity.post.PostType;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

public record PostResponse(
		UUID id,
		ForumResponse forum,
		UUID authorId,
		UUID projectId,
		UUID courseId,
		String title,
		String content,
		PostType type,
		ContentStatus status,
		boolean commentsLocked,
		Set<TagResponse> tags,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt
) {
}
