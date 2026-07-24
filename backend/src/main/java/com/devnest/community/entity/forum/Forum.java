package com.devnest.community.entity.forum;

import com.devnest.common.entity.BaseEntity;
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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
		name = "community_forums",
		indexes = @Index(name = "idx_community_forums_status_name", columnList = "status, name")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Forum extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "created_by_id", nullable = false)
	private User createdBy;

	@Column(nullable = false, length = 80)
	private String name;

	@Column(nullable = false, unique = true, length = 100)
	private String slug;

	@Column(nullable = false, length = 500)
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private ForumStatus status;

	public static Forum create(User createdBy, String name, String slug, String description) {
		Forum forum = new Forum();
		forum.createdBy = createdBy;
		forum.name = name;
		forum.slug = slug;
		forum.description = description;
		forum.status = ForumStatus.ACTIVE;
		return forum;
	}

	public void update(String name, String slug, String description) {
		this.name = name;
		this.slug = slug;
		this.description = description;
	}

	public void archive() {
		this.status = ForumStatus.ARCHIVED;
	}

	public void restore() {
		this.status = ForumStatus.ACTIVE;
	}

	public boolean isActive() {
		return this.status == ForumStatus.ACTIVE;
	}
}
