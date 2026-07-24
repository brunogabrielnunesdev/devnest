package com.devnest.community.service.ratelimit;

import com.devnest.community.config.CommunityLimitsProperties;
import com.devnest.community.entity.ratelimit.RateLimitAction;
import com.devnest.community.entity.ratelimit.RateLimitEvent;
import com.devnest.community.exception.ratelimit.CommunityRateLimitExceededException;
import com.devnest.community.repository.ratelimit.RateLimitEventRepository;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommunityRateLimitService {

	private final RateLimitEventRepository eventRepository;
	private final CommunityLimitsProperties limits;
	private final Clock communityClock;

	public void validateCommentCreation(UUID authorId) {
		consume(authorId, RateLimitAction.COMMENT_CREATE, "comments", limits.getCommentsPerMinute());
	}

	public void validateReactionChange(UUID userId) {
		consume(userId, RateLimitAction.REACTION_CHANGE, "reactions", limits.getReactionsPerMinute());
	}

	private void consume(UUID actorId, RateLimitAction action, String resource, int limit) {
		OffsetDateTime windowStart = OffsetDateTime.now(communityClock).minusMinutes(1);
		long recentEvents = eventRepository
				.countByActorIdAndActionAndCreatedAtGreaterThanEqual(actorId, action, windowStart);
		if (recentEvents >= limit) {
			throw new CommunityRateLimitExceededException(resource, limit);
		}
		eventRepository.saveAndFlush(RateLimitEvent.create(actorId, action));
	}
}
