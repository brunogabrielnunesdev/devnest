package com.devnest.community.entity.comment;

import com.devnest.common.entity.BaseEntity;
import com.devnest.community.entity.post.ContentStatus;
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
import java.time.OffsetDateTime;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "CommunityComment")
@Getter
@Table(name = "community_comments", indexes = {
		@Index(name = "idx_community_comments_post_feed", columnList = "post_id, status, created_at, id"),
		@Index(name = "idx_community_comments_author", columnList = "author_id, status, created_at")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "post_id", nullable = false)
	private Post post;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "author_id", nullable = false)
	private User author;

	@Column(nullable = false, length = 5000)
	private String content;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private ContentStatus status;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "removed_by_id")
	private User removedBy;

	@Column(name = "removed_at")
	private OffsetDateTime removedAt;

	@Column(name = "removal_reason", length = 500)
	private String removalReason;

	@Column(name = "content_filter_rule_version", length = 100)
	private String contentFilterRuleVersion;

	@Column(name = "content_filter_matched_terms", columnDefinition = "text")
	private String contentFilterMatchedTerms;

	public static Comment create(Post post, User author, String content) {
		Comment comment = new Comment();
		comment.post = post;
		comment.author = author;
		comment.content = content;
		comment.status = ContentStatus.ACTIVE;
		return comment;
	}

	public void update(String content) {
		this.content = content;
	}

	public void applyContentFilter(boolean requiresReview, String ruleVersion, Set<String> matchedTerms) {
		if (requiresReview) {
			this.status = ContentStatus.HELD_FOR_REVIEW;
			this.contentFilterRuleVersion = ruleVersion;
			this.contentFilterMatchedTerms = String.join("\n", matchedTerms);
			return;
		}
		this.status = ContentStatus.ACTIVE;
		this.contentFilterRuleVersion = null;
		this.contentFilterMatchedTerms = null;
	}

	public void remove(User removedBy, String reason, OffsetDateTime removedAt) {
		this.status = ContentStatus.REMOVED;
		this.removedBy = removedBy;
		this.removalReason = reason;
		this.removedAt = removedAt;
	}

	public void hide() {
		this.status = ContentStatus.HIDDEN;
	}

	public void activate() {
		this.status = ContentStatus.ACTIVE;
		this.removedBy = null;
		this.removedAt = null;
		this.removalReason = null;
	}
}
