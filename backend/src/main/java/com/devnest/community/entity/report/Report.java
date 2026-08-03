package com.devnest.community.entity.report;

import com.devnest.common.entity.BaseEntity;
import com.devnest.community.entity.comment.Comment;
import com.devnest.community.entity.post.Post;
import com.devnest.identity.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "CommunityReport")
@Getter
@Table(name = "community_reports", indexes = {
		@Index(name = "idx_community_reports_queue", columnList = "status, created_at, id"),
		@Index(name = "idx_community_reports_post", columnList = "post_id, status"),
		@Index(name = "idx_community_reports_comment", columnList = "comment_id, status")
}, uniqueConstraints = {
		@UniqueConstraint(
				name = "uk_community_reports_reporter_post",
				columnNames = {"reporter_id", "post_id"}
		),
		@UniqueConstraint(
				name = "uk_community_reports_reporter_comment",
				columnNames = {"reporter_id", "comment_id"}
		)
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "reporter_id", nullable = false)
	private User reporter;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "post_id")
	private Post post;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "comment_id")
	private Comment comment;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 40)
	private ReportReason reason;

	@Column(length = 1000)
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private ReportStatus status;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reviewed_by_id")
	private User reviewedBy;

	@Column(name = "reviewed_at")
	private OffsetDateTime reviewedAt;

	@Column(name = "review_note", length = 1000)
	private String reviewNote;

	public static Report forPost(
			User reporter,
			Post post,
			ReportReason reason,
			String description
	) {
		Report report = create(reporter, reason, description);
		report.post = post;
		return report;
	}

	public static Report forComment(
			User reporter,
			Comment comment,
			ReportReason reason,
			String description
	) {
		Report report = create(reporter, reason, description);
		report.comment = comment;
		return report;
	}

	private static Report create(User reporter, ReportReason reason, String description) {
		Report report = new Report();
		report.reporter = reporter;
		report.reason = reason;
		report.description = description;
		report.status = ReportStatus.PENDING;
		return report;
	}

	public void review(
			ReportDecision decision,
			User reviewer,
			String note,
			OffsetDateTime reviewedAt
	) {
		if (status != ReportStatus.PENDING) {
			throw new IllegalStateException("Only pending reports can be reviewed.");
		}
		status = decision == ReportDecision.CONFIRM
				? ReportStatus.CONFIRMED
				: ReportStatus.DISMISSED;
		reviewedBy = reviewer;
		reviewNote = note;
		this.reviewedAt = reviewedAt;
	}
}
