package com.devnest.community.repository.ratelimit;

import com.devnest.community.entity.ratelimit.RateLimitAction;
import com.devnest.community.entity.ratelimit.RateLimitEvent;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RateLimitEventRepository extends JpaRepository<RateLimitEvent, UUID> {

	long countByActorIdAndActionAndCreatedAtGreaterThanEqual(
			UUID actorId,
			RateLimitAction action,
			OffsetDateTime createdAfter
	);
}
