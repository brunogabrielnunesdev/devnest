package com.devnest.community.service.userrelation;

import com.devnest.community.exception.access.CommunityForbiddenException;
import com.devnest.community.repository.userrelation.UserBlockRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserRelationAccessService {

	private final UserBlockRepository userBlockRepository;

	public void validateInteraction(UUID actorId, UUID contentAuthorId) {
		if (!actorId.equals(contentAuthorId)
				&& userBlockRepository.existsBetween(actorId, contentAuthorId)) {
			throw new CommunityForbiddenException("Interaction is not allowed between blocked users.");
		}
	}
}
