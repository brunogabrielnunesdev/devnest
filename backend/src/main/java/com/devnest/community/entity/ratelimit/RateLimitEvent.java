package com.devnest.community.entity.ratelimit;

import com.devnest.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity(name = "CommunityRateLimitEvent")
@Table(name = "community_rate_limit_events", indexes = {
		@Index(
				name = "idx_community_rate_limit_actor_action_created",
				columnList = "actor_id, action, created_at"
		)
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RateLimitEvent extends BaseEntity {

	@Column(name = "actor_id", nullable = false)
	private UUID actorId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 40)
	private RateLimitAction action;

	public static RateLimitEvent create(UUID actorId, RateLimitAction action) {
		RateLimitEvent event = new RateLimitEvent();
		event.actorId = actorId;
		event.action = action;
		return event;
	}
}
