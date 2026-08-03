package com.devnest.community.entity.moderation;

import com.devnest.common.entity.BaseEntity;
import com.devnest.community.entity.comment.Comment;
import com.devnest.community.entity.post.Post;
import com.devnest.community.entity.report.Report;
import com.devnest.identity.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "CommunityModerationCase")
@Getter
@Table(name = "community_moderation_cases", indexes = {
		@Index(name = "idx_community_moderation_cases_queue", columnList = "status, created_at, id")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ModerationCase extends BaseEntity {

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "report_id", nullable = false, unique = true)
	private Report report;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "post_id")
	private Post post;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "comment_id")
	private Comment comment;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private ModerationCaseStatus status;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "opened_by_id", nullable = false)
	private User openedBy;

	@Column(name = "opened_at", nullable = false)
	private OffsetDateTime openedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "resolved_by_id")
	private User resolvedBy;

	@Column(name = "resolved_at")
	private OffsetDateTime resolvedAt;

	public static ModerationCase open(Report report, User moderator, OffsetDateTime openedAt) {
		ModerationCase moderationCase = new ModerationCase();
		moderationCase.report = report;
		moderationCase.post = report.getPost();
		moderationCase.comment = report.getComment();
		moderationCase.status = ModerationCaseStatus.OPEN;
		moderationCase.openedBy = moderator;
		moderationCase.openedAt = openedAt;
		return moderationCase;
	}

	public void resolve(User moderator, OffsetDateTime resolvedAt) {
		status = ModerationCaseStatus.RESOLVED;
		resolvedBy = moderator;
		this.resolvedAt = resolvedAt;
	}
}
