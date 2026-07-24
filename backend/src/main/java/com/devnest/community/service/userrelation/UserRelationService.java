package com.devnest.community.service.userrelation;

import com.devnest.community.dto.userrelation.UserRelationResponse;
import com.devnest.community.entity.userrelation.UserBlock;
import com.devnest.community.entity.userrelation.UserMute;
import com.devnest.community.exception.reference.ReferenceNotFoundException;
import com.devnest.community.exception.userrelation.SelfRelationException;
import com.devnest.community.repository.userrelation.UserBlockRepository;
import com.devnest.community.repository.userrelation.UserMuteRepository;
import com.devnest.community.service.access.AccessService;
import com.devnest.identity.entity.User;
import com.devnest.identity.entity.UserStatus;
import com.devnest.identity.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserRelationService {

	private final AccessService accessService;
	private final UserRepository userRepository;
	private final UserBlockRepository userBlockRepository;
	private final UserMuteRepository userMuteRepository;

	@Transactional
	public UserRelationResponse block(UUID targetId) {
		User current = accessService.getAuthenticatedUser();
		User target = findTarget(current, targetId);
		UserBlock block = userBlockRepository.findByBlockerIdAndBlockedUserId(current.getId(), targetId)
				.orElseGet(() -> userBlockRepository.save(UserBlock.create(current, target)));
		return response(block.getBlockedUser(), block.getCreatedAt());
	}

	@Transactional
	public void unblock(UUID targetId) {
		User current = accessService.getAuthenticatedUser();
		validateNotSelf(current.getId(), targetId);
		userBlockRepository.findByBlockerIdAndBlockedUserId(current.getId(), targetId)
				.ifPresent(userBlockRepository::delete);
	}

	@Transactional(readOnly = true)
	public Page<UserRelationResponse> findBlocked(Pageable pageable) {
		UUID currentId = accessService.getAuthenticatedUser().getId();
		return userBlockRepository.findAllByBlockerId(currentId, pageable)
				.map(block -> response(block.getBlockedUser(), block.getCreatedAt()));
	}

	@Transactional
	public UserRelationResponse mute(UUID targetId) {
		User current = accessService.getAuthenticatedUser();
		User target = findTarget(current, targetId);
		UserMute mute = userMuteRepository.findByUserIdAndMutedUserId(current.getId(), targetId)
				.orElseGet(() -> userMuteRepository.save(UserMute.create(current, target)));
		return response(mute.getMutedUser(), mute.getCreatedAt());
	}

	@Transactional
	public void unmute(UUID targetId) {
		User current = accessService.getAuthenticatedUser();
		validateNotSelf(current.getId(), targetId);
		userMuteRepository.findByUserIdAndMutedUserId(current.getId(), targetId)
				.ifPresent(userMuteRepository::delete);
	}

	@Transactional(readOnly = true)
	public Page<UserRelationResponse> findMuted(Pageable pageable) {
		UUID currentId = accessService.getAuthenticatedUser().getId();
		return userMuteRepository.findAllByUserId(currentId, pageable)
				.map(mute -> response(mute.getMutedUser(), mute.getCreatedAt()));
	}

	private User findTarget(User current, UUID targetId) {
		validateNotSelf(current.getId(), targetId);
		return userRepository.findByIdAndStatus(targetId, UserStatus.ACTIVE)
				.orElseThrow(() -> new ReferenceNotFoundException("User"));
	}

	private void validateNotSelf(UUID currentId, UUID targetId) {
		if (currentId.equals(targetId)) {
			throw new SelfRelationException();
		}
	}

	private UserRelationResponse response(User user, java.time.OffsetDateTime createdAt) {
		return new UserRelationResponse(user.getId(), user.getProfile().getDisplayName(), createdAt);
	}
}
