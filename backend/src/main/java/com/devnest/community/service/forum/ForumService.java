package com.devnest.community.service.forum;

import com.devnest.community.dto.forum.ForumRequest;
import com.devnest.community.dto.forum.ForumResponse;
import com.devnest.community.entity.forum.Forum;
import com.devnest.community.entity.forum.ForumStatus;
import com.devnest.community.exception.forum.ForumNotFoundException;
import com.devnest.community.exception.slug.SlugConflictException;
import com.devnest.community.mapper.forum.ForumMapper;
import com.devnest.community.repository.forum.ForumRepository;
import com.devnest.community.service.access.AccessService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ForumService {

	private final AccessService accessService;
	private final ForumRepository forumRepository;
	private final ForumMapper forumMapper;

	@Transactional
	public ForumResponse create(ForumRequest request) {
		var admin = accessService.getAuthenticatedAdmin();
		validateSlugAvailable(request.slug(), null);
		Forum forum = Forum.create(
				admin,
				request.name(),
				request.slug(),
				request.description()
		);
		return forumMapper.toResponse(forumRepository.save(forum));
	}

	@Transactional(readOnly = true)
	public Page<ForumResponse> findActive(Pageable pageable) {
		return forumRepository.findAllByStatus(ForumStatus.ACTIVE, pageable)
				.map(forumMapper::toResponse);
	}

	@Transactional(readOnly = true)
	public ForumResponse findActiveBySlug(String slug) {
		Forum forum = forumRepository.findBySlugAndStatus(slug, ForumStatus.ACTIVE)
				.orElseThrow(ForumNotFoundException::new);
		return forumMapper.toResponse(forum);
	}

	@Transactional
	public ForumResponse update(UUID forumId, ForumRequest request) {
		accessService.getAuthenticatedAdmin();
		Forum forum = findForum(forumId);
		validateSlugAvailable(request.slug(), forumId);
		forum.update(request.name(), request.slug(), request.description());
		return forumMapper.toResponse(forum);
	}

	@Transactional
	public ForumResponse archive(UUID forumId) {
		accessService.getAuthenticatedAdmin();
		Forum forum = findForum(forumId);
		forum.archive();
		return forumMapper.toResponse(forum);
	}

	@Transactional
	public ForumResponse restore(UUID forumId) {
		accessService.getAuthenticatedAdmin();
		Forum forum = findForum(forumId);
		forum.restore();
		return forumMapper.toResponse(forum);
	}

	private Forum findForum(UUID forumId) {
		return forumRepository.findById(forumId)
				.orElseThrow(ForumNotFoundException::new);
	}

	private void validateSlugAvailable(String slug, UUID currentForumId) {
		forumRepository.findBySlug(slug).ifPresent(existingForum -> {
			if (currentForumId == null || !existingForum.getId().equals(currentForumId)) {
				throw new SlugConflictException();
			}
		});
	}
}
