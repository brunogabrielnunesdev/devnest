package com.devnest.community.service.concurrency;

import com.devnest.common.exception.ResourceNotFoundException;
import com.devnest.identity.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommunityActorLockService {

	private final UserRepository userRepository;

	public void lock(UUID actorId) {
		userRepository.findByIdForUpdate(actorId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found."));
	}
}
