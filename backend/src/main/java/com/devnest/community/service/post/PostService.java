package com.devnest.community.service.post;

import com.devnest.community.config.CommunityLimitsProperties;
import com.devnest.community.dto.post.PostRequest;
import com.devnest.community.dto.post.PostResponse;
import com.devnest.community.dto.post.PostUpdateRequest;
import com.devnest.community.entity.forum.Forum;
import com.devnest.community.entity.post.ContentStatus;
import com.devnest.community.entity.post.Post;
import com.devnest.community.entity.tag.CommunityTag;
import com.devnest.community.exception.forum.ForumNotFoundException;
import com.devnest.community.exception.forum.ForumUnavailableException;
import com.devnest.community.exception.post.PostLimitExceededException;
import com.devnest.community.exception.post.PostNotFoundException;
import com.devnest.community.exception.post.PostUnavailableException;
import com.devnest.community.exception.reference.ReferenceNotFoundException;
import com.devnest.community.exception.tag.TagNotFoundException;
import com.devnest.community.mapper.post.PostMapper;
import com.devnest.community.repository.forum.ForumRepository;
import com.devnest.community.repository.post.PostRepository;
import com.devnest.community.repository.tag.TagRepository;
import com.devnest.community.service.access.AccessService;
import com.devnest.community.service.content.ContentFilter;
import com.devnest.community.service.content.ContentFilterResult;
import com.devnest.course.entity.course.Course;
import com.devnest.course.repository.course.CourseRepository;
import com.devnest.project.entity.project.Project;
import com.devnest.project.repository.project.ProjectRepository;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

	private final AccessService accessService;
	private final ForumRepository forumRepository;
	private final PostRepository postRepository;
	private final TagRepository tagRepository;
	private final ProjectRepository projectRepository;
	private final CourseRepository courseRepository;
	private final PostMapper postMapper;
	private final ContentFilter contentFilter;
	private final Clock communityClock;
	private final CommunityLimitsProperties limits;

	@Transactional
	public PostResponse create(UUID forumId, PostRequest request) {
		var author = accessService.getAuthenticatedUser();
		Forum forum = findActiveForum(forumId);
		validatePostLimit(author.getId());
		Post post = Post.create(
				forum,
				author,
				request.title(),
				request.content(),
				request.type(),
				findProject(request.projectId()),
				findCourse(request.courseId())
		);
		applyContentFilter(post, request.title(), request.content());
		post.replaceTags(findTags(request.tagIds()));
		return postMapper.toResponse(postRepository.save(post));
	}

	@Transactional(readOnly = true)
	public Page<PostResponse> findFeed(Pageable pageable) {
		UUID viewerId = accessService.getAuthenticatedUser().getId();
		return postRepository.findFeedForUser(viewerId, ContentStatus.ACTIVE, pageable)
				.map(postMapper::toResponse);
	}

	@Transactional(readOnly = true)
	public Page<PostResponse> findForumFeed(UUID forumId, Pageable pageable) {
		findActiveForum(forumId);
		UUID viewerId = accessService.getAuthenticatedUser().getId();
		return postRepository.findForumFeedForUser(
				forumId,
				viewerId,
				ContentStatus.ACTIVE,
				pageable
		).map(postMapper::toResponse);
	}

	@Transactional(readOnly = true)
	public PostResponse findById(UUID postId) {
		Post post = postRepository.findByIdAndStatus(postId, ContentStatus.ACTIVE)
				.orElseThrow(PostNotFoundException::new);
		return postMapper.toResponse(post);
	}

	@Transactional
	public PostResponse update(UUID postId, PostUpdateRequest request) {
		Post post = accessService.getPostForManagement(postId);
		validatePostActive(post);
		post.update(
				findActiveForum(request.forumId()),
				request.title(),
				request.content(),
				request.type(),
				findProject(request.projectId()),
				findCourse(request.courseId())
		);
		applyContentFilter(post, request.title(), request.content());
		post.replaceTags(findTags(request.tagIds()));
		return postMapper.toResponse(post);
	}

	@Transactional
	public void remove(UUID postId, String reason) {
		Post post = accessService.getPostForManagement(postId);
		validatePostActive(post);
		post.remove(accessService.getAuthenticatedUser(), reason, OffsetDateTime.now(communityClock));
	}

	private Forum findActiveForum(UUID forumId) {
		Forum forum = forumRepository.findById(forumId)
				.orElseThrow(ForumNotFoundException::new);
		if (!forum.isActive()) {
			throw new ForumUnavailableException();
		}
		return forum;
	}

	private Project findProject(UUID projectId) {
		if (projectId == null) {
			return null;
		}
		return projectRepository.findById(projectId)
				.orElseThrow(() -> new ReferenceNotFoundException("Project"));
	}

	private Course findCourse(UUID courseId) {
		if (courseId == null) {
			return null;
		}
		return courseRepository.findById(courseId)
				.orElseThrow(() -> new ReferenceNotFoundException("Course"));
	}

	private Set<CommunityTag> findTags(Set<UUID> tagIds) {
		if (tagIds == null || tagIds.isEmpty()) {
			return Set.of();
		}
		Set<CommunityTag> tags = new LinkedHashSet<>(tagRepository.findAllById(tagIds));
		if (tags.size() != tagIds.size()) {
			throw new TagNotFoundException();
		}
		return tags;
	}

	private void validatePostLimit(UUID authorId) {
		OffsetDateTime windowStart = OffsetDateTime.now(communityClock).minusHours(24);
		if (postRepository.countByAuthorIdAndCreatedAtGreaterThanEqual(authorId, windowStart)
				>= limits.getPostsPer24Hours()) {
			throw new PostLimitExceededException();
		}
	}

	private void validatePostActive(Post post) {
		if (post.getStatus() != ContentStatus.ACTIVE
				&& post.getStatus() != ContentStatus.HELD_FOR_REVIEW) {
			throw new PostUnavailableException();
		}
	}

	private void applyContentFilter(Post post, String title, String content) {
		ContentFilterResult result = contentFilter.evaluate(title + "\n" + content);
		post.applyContentFilter(result.requiresReview(), result.ruleVersion(), result.matchedTerms());
	}
}
