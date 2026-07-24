package com.devnest.community.entity.reaction;

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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "CommunityReaction")
@Getter
@Table(name = "community_reactions", indexes = {
		@Index(name = "idx_community_reactions_post_type", columnList = "post_id, type"),
		@Index(name = "idx_community_reactions_comment_type", columnList = "comment_id, type")
}, uniqueConstraints = {
		@UniqueConstraint(name = "uk_community_reactions_user_post", columnNames = {"user_id", "post_id"}),
		@UniqueConstraint(name = "uk_community_reactions_user_comment", columnNames = {"user_id", "comment_id"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reaction extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "post_id")
	private Post post;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "comment_id")
	private Comment comment;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private ReactionType type;

	public static Reaction forPost(User user, Post post, ReactionType type) {
		Reaction reaction = new Reaction();
		reaction.user = user;
		reaction.post = post;
		reaction.type = type;
		return reaction;
	}

	public static Reaction forComment(User user, Comment comment, ReactionType type) {
		Reaction reaction = new Reaction();
		reaction.user = user;
		reaction.comment = comment;
		reaction.type = type;
		return reaction;
	}

	public void changeType(ReactionType type) {
		this.type = type;
	}
}
