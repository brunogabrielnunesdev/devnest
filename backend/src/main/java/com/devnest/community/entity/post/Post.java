package com.devnest.community.entity.post;

import com.devnest.common.entity.BaseEntity;
import com.devnest.community.entity.forum.Forum;
import com.devnest.community.entity.tag.CommunityTag;
import com.devnest.course.entity.course.Course;
import com.devnest.identity.entity.User;
import com.devnest.project.entity.project.Project;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
		name = "community_posts",
		indexes = {
				@Index(name = "idx_community_posts_feed", columnList = "status, created_at, id"),
				@Index(name = "idx_community_posts_forum_feed", columnList = "forum_id, status, created_at, id"),
				@Index(name = "idx_community_posts_author", columnList = "author_id, status, created_at"),
				@Index(name = "idx_community_posts_project", columnList = "project_id"),
				@Index(name = "idx_community_posts_course", columnList = "course_id")
		}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "forum_id", nullable = false)
	private Forum forum;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "author_id", nullable = false)
	private User author;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_id")
	private Project project;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "course_id")
	private Course course;

	@Column(nullable = false, length = 160)
	private String title;

	@Column(nullable = false, columnDefinition = "text")
	private String content;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private PostType type;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private ContentStatus status;

	@Column(name = "comments_locked", nullable = false)
	private boolean commentsLocked;

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

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
			name = "community_post_tags",
			joinColumns = @JoinColumn(name = "post_id"),
			inverseJoinColumns = @JoinColumn(name = "tag_id"),
			uniqueConstraints = @UniqueConstraint(
					name = "uk_community_post_tags_post_tag",
					columnNames = {"post_id", "tag_id"}
			),
			indexes = @Index(name = "idx_community_post_tags_tag_post", columnList = "tag_id, post_id")
	)
	private Set<CommunityTag> tags = new LinkedHashSet<>();

	public static Post create(
			Forum forum,
			User author,
			String title,
			String content,
			PostType type,
			Project project,
			Course course
	) {
		Post post = new Post();
		post.forum = forum;
		post.author = author;
		post.title = title;
		post.content = content;
		post.type = type;
		post.project = project;
		post.course = course;
		post.status = ContentStatus.ACTIVE;
		post.commentsLocked = false;
		return post;
	}

	public void update(
			Forum forum,
			String title,
			String content,
			PostType type,
			Project project,
			Course course
	) {
		this.forum = forum;
		this.title = title;
		this.content = content;
		this.type = type;
		this.project = project;
		this.course = course;
	}

	public void replaceTags(Set<CommunityTag> tags) {
		this.tags.clear();
		this.tags.addAll(tags);
	}

	public void holdForReview() {
		this.status = ContentStatus.HELD_FOR_REVIEW;
		this.commentsLocked = true;
	}

	public void applyContentFilter(boolean requiresReview, String ruleVersion, Set<String> matchedTerms) {
		if (requiresReview) {
			holdForReview();
			this.contentFilterRuleVersion = ruleVersion;
			this.contentFilterMatchedTerms = String.join("\n", matchedTerms);
			return;
		}
		this.status = ContentStatus.ACTIVE;
		this.commentsLocked = false;
		this.contentFilterRuleVersion = null;
		this.contentFilterMatchedTerms = null;
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

	public void remove(User removedBy, String removalReason, OffsetDateTime removedAt) {
		this.status = ContentStatus.REMOVED;
		this.commentsLocked = true;
		this.removedBy = removedBy;
		this.removalReason = removalReason;
		this.removedAt = removedAt;
	}

	public void lockComments() {
		this.commentsLocked = true;
	}

	public void unlockComments() {
		this.commentsLocked = false;
	}
}
