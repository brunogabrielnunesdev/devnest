package com.devnest.community.entity.moderation;

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
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "CommunityModerationAction")
@Getter
@Table(name = "community_moderation_actions", indexes = {
		@Index(name = "idx_community_moderation_actions_case", columnList = "case_id, created_at, id")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ModerationAction extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "case_id", nullable = false)
	private ModerationCase moderationCase;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private ModerationActionType actionType;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "moderator_id", nullable = false)
	private User moderator;

	@Column(nullable = false, length = 1000)
	private String reason;

	@Column(name = "previous_state", nullable = false, length = 100)
	private String previousState;

	@Column(name = "new_state", nullable = false, length = 100)
	private String newState;

	@Column(name = "performed_at", nullable = false)
	private OffsetDateTime performedAt;

	public static ModerationAction create(
			ModerationCase moderationCase,
			ModerationActionType actionType,
			User moderator,
			String reason,
			String previousState,
			String newState,
			OffsetDateTime performedAt
	) {
		ModerationAction action = new ModerationAction();
		action.moderationCase = moderationCase;
		action.actionType = actionType;
		action.moderator = moderator;
		action.reason = reason;
		action.previousState = previousState;
		action.newState = newState;
		action.performedAt = performedAt;
		return action;
	}
}
