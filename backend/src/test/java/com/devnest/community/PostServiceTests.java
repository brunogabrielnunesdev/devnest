package com.devnest.community;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devnest.auth.security.useridentity.CustomAuthentication;
import com.devnest.community.dto.forum.ForumRequest;
import com.devnest.community.dto.post.PostRequest;
import com.devnest.community.dto.post.PostUpdateRequest;
import com.devnest.community.entity.post.ContentStatus;
import com.devnest.community.entity.post.PostType;
import com.devnest.community.entity.tag.CommunityTag;
import com.devnest.community.exception.access.CommunityForbiddenException;
import com.devnest.community.exception.forum.ForumUnavailableException;
import com.devnest.community.exception.post.PostLimitExceededException;
import com.devnest.community.exception.post.PostNotFoundException;
import com.devnest.community.exception.tag.TagNotFoundException;
import com.devnest.community.repository.post.PostRepository;
import com.devnest.community.repository.tag.TagRepository;
import com.devnest.community.service.forum.ForumService;
import com.devnest.community.service.post.PostService;
import com.devnest.identity.entity.User;
import com.devnest.identity.entity.UserRole;
import com.devnest.identity.repository.UserRepository;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@TestPropertySource(properties = {
		"devnest.community.content-filter.rule-version=test-post-filter-v1",
		"devnest.community.content-filter.bad-words[0]=idiota"
})
class PostServiceTests {

	@Autowired
	private PostService postService;

	@Autowired
	private ForumService forumService;

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private TagRepository tagRepository;

	@Autowired
	private UserRepository userRepository;

	@AfterEach
	void clearAuthentication() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void authenticatedUserCreatesPostInActiveForum() {
		UUID forumId = createForum("create-post");
		User author = saveStudent("post-author");
		authenticate(author);

		var response = postService.create(forumId, request("First post", Set.of()));

		assertThat(response.id()).isNotNull();
		assertThat(response.authorId()).isEqualTo(author.getId());
		assertThat(response.forum().id()).isEqualTo(forumId);
		assertThat(response.status()).isEqualTo(ContentStatus.ACTIVE);
		assertThat(postRepository.findById(response.id())).isPresent();
	}

	@Test
	void suspiciousPostIsHeldAndDoesNotAppearInPublicFeed() {
		UUID forumId = createForum("held-post");
		authenticate(saveStudent("held-post-author"));

		var response = postService.create(
				forumId,
				requestWithContent("Suspicious post", "Seu conteúdo é idiota", Set.of())
		);

		assertThat(response.status()).isEqualTo(ContentStatus.HELD_FOR_REVIEW);
		assertThat(response.commentsLocked()).isTrue();
		assertThat(postService.findFeed(PageRequest.of(0, 20)).getContent())
				.noneMatch(item -> item.id().equals(response.id()));
		var persisted = postRepository.findById(response.id()).orElseThrow();
		assertThat(persisted.getContentFilterRuleVersion()).isEqualTo("test-post-filter-v1");
		assertThat(persisted.getContentFilterMatchedTerms()).contains("idiota");
	}

	@Test
	void authorCanCorrectHeldPostAndReturnItToActiveFeed() {
		UUID forumId = createForum("correct-held-post");
		authenticate(saveStudent("correct-held-author"));
		var held = postService.create(
				forumId,
				requestWithContent("Suspicious post", "Seu conteúdo é idiota", Set.of())
		);

		var corrected = postService.update(
				held.id(),
				updateRequestWithContent(forumId, "Corrected post", "Conteúdo respeitoso", Set.of())
		);

		assertThat(corrected.status()).isEqualTo(ContentStatus.ACTIVE);
		assertThat(corrected.commentsLocked()).isFalse();
		assertThat(postService.findFeed(PageRequest.of(0, 20)).getContent())
				.anyMatch(item -> item.id().equals(held.id()));
		var persisted = postRepository.findById(held.id()).orElseThrow();
		assertThat(persisted.getContentFilterRuleVersion()).isNull();
		assertThat(persisted.getContentFilterMatchedTerms()).isNull();
	}

	@Test
	void postCannotBeCreatedInArchivedForum() {
		UUID forumId = createForum("archived-forum");
		forumService.archive(forumId);
		authenticate(saveStudent("archived-author"));

		assertThatThrownBy(() -> postService.create(forumId, request("Post", Set.of())))
				.isInstanceOf(ForumUnavailableException.class);
	}

	@Test
	void userCannotUpdateAnotherAuthorsPost() {
		UUID forumId = createForum("protected-post");
		User author = saveStudent("protected-author");
		authenticate(author);
		var post = postService.create(forumId, request("Original", Set.of()));
		authenticate(saveStudent("other-user"));

		assertThatThrownBy(() -> postService.update(
				post.id(),
				updateRequest(forumId, "Changed", Set.of())
		))
				.isInstanceOf(CommunityForbiddenException.class)
				.hasMessage("Only the post author or an admin can manage this post.");
	}

	@Test
	void authorUpdatesPostAndReplacesTags() {
		UUID forumId = createForum("update-post");
		CommunityTag oldTag = tagRepository.save(CommunityTag.create("Java", uniqueSlug("java")));
		CommunityTag newTag = tagRepository.save(CommunityTag.create("Spring", uniqueSlug("spring")));
		User author = saveStudent("update-author");
		authenticate(author);
		var post = postService.create(forumId, request("Original", Set.of(oldTag.getId())));

		var updated = postService.update(
				post.id(),
				updateRequest(forumId, "Updated", Set.of(newTag.getId()))
		);

		assertThat(updated.title()).isEqualTo("Updated");
		assertThat(updated.tags()).extracting(tag -> tag.id()).containsExactly(newTag.getId());
	}

	@Test
	void missingTagIsRejected() {
		UUID forumId = createForum("missing-tag");
		authenticate(saveStudent("missing-tag-author"));

		assertThatThrownBy(() -> postService.create(
				forumId,
				request("Post", Set.of(UUID.randomUUID()))
		))
				.isInstanceOf(TagNotFoundException.class);
	}

	@Test
	void sixthPostWithinTwentyFourHoursIsRejected() {
		UUID forumId = createForum("post-limit");
		authenticate(saveStudent("limited-author"));
		for (int index = 1; index <= 5; index++) {
			postService.create(forumId, request("Post " + index, Set.of()));
		}

		assertThatThrownBy(() -> postService.create(forumId, request("Post 6", Set.of())))
				.isInstanceOf(PostLimitExceededException.class);
	}

	@Test
	void removedPostDisappearsFromFeedAndPublicLookup() {
		UUID forumId = createForum("remove-post");
		authenticate(saveStudent("remove-author"));
		var post = postService.create(forumId, request("Remove me", Set.of()));

		postService.remove(post.id(), "Removed by author");

		assertThat(postService.findFeed(PageRequest.of(0, 20)).getContent())
				.noneMatch(item -> item.id().equals(post.id()));
		assertThatThrownBy(() -> postService.findById(post.id()))
				.isInstanceOf(PostNotFoundException.class);
		var persisted = postRepository.findById(post.id()).orElseThrow();
		assertThat(persisted.getStatus()).isEqualTo(ContentStatus.REMOVED);
		assertThat(persisted.getRemovedAt()).isNotNull();
		assertThat(persisted.isCommentsLocked()).isTrue();
	}

	private UUID createForum(String prefix) {
		User admin = saveAdmin(prefix + "-admin");
		authenticate(admin);
		String slug = uniqueSlug(prefix);
		return forumService.create(new ForumRequest(
				"Forum " + prefix,
				slug,
				"Forum description"
		)).id();
	}

	private PostRequest request(String title, Set<UUID> tagIds) {
		return requestWithContent(title, "Post content", tagIds);
	}

	private PostRequest requestWithContent(String title, String content, Set<UUID> tagIds) {
		return new PostRequest(
				title,
				content,
				PostType.DISCUSSION,
				null,
				null,
				tagIds
		);
	}

	private PostUpdateRequest updateRequest(UUID forumId, String title, Set<UUID> tagIds) {
		return updateRequestWithContent(forumId, title, "Updated content", tagIds);
	}

	private PostUpdateRequest updateRequestWithContent(
			UUID forumId,
			String title,
			String content,
			Set<UUID> tagIds
	) {
		return new PostUpdateRequest(
				forumId,
				title,
				content,
				PostType.QUESTION,
				null,
				null,
				tagIds
		);
	}

	private User saveStudent(String prefix) {
		return userRepository.save(User.createStudent(uniqueEmail(prefix), "password-hash", "Student"));
	}

	private User saveAdmin(String prefix) {
		User admin = User.createStudent(uniqueEmail(prefix), "password-hash", "Admin");
		admin.setRole(UserRole.ADMIN);
		return userRepository.save(admin);
	}

	private void authenticate(User user) {
		CustomAuthentication principal = new CustomAuthentication(user);
		var authentication = new UsernamePasswordAuthenticationToken(
				principal,
				null,
				principal.getAuthorities()
		);
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	private String uniqueEmail(String prefix) {
		return prefix + "-" + UUID.randomUUID() + "@example.com";
	}

	private String uniqueSlug(String prefix) {
		return prefix + "-" + UUID.randomUUID();
	}
}
