package com.devnest.community;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devnest.auth.security.useridentity.CustomAuthentication;
import com.devnest.community.dto.comment.CommentRequest;
import com.devnest.community.dto.forum.ForumRequest;
import com.devnest.community.dto.post.PostRequest;
import com.devnest.community.entity.post.ContentStatus;
import com.devnest.community.entity.post.PostType;
import com.devnest.community.exception.access.CommunityForbiddenException;
import com.devnest.community.exception.comment.CommentUnavailableException;
import com.devnest.community.exception.duplicate.DuplicateContentException;
import com.devnest.community.exception.ratelimit.CommunityRateLimitExceededException;
import com.devnest.community.repository.comment.CommentRepository;
import com.devnest.community.repository.post.PostRepository;
import com.devnest.community.service.comment.CommentService;
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
		"devnest.community.content-filter.rule-version=test-comment-filter-v1",
		"devnest.community.content-filter.bad-words[0]=idiota",
		"devnest.community.limits.comments-per-minute=3"
})
class CommentServiceTests {

	@Autowired private CommentService commentService;
	@Autowired private PostService postService;
	@Autowired private ForumService forumService;
	@Autowired private CommentRepository commentRepository;
	@Autowired private PostRepository postRepository;
	@Autowired private UserRepository userRepository;

	@AfterEach
	void clearAuthentication() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void authenticatedUserCommentsAndListsActivePost() {
		UUID postId = createPost("comment-create");
		User author = saveStudent("comment-author");
		authenticate(author);

		var response = commentService.create(postId, new CommentRequest("Helpful comment"));

		assertThat(response.authorId()).isEqualTo(author.getId());
		assertThat(response.status()).isEqualTo(ContentStatus.ACTIVE);
		assertThat(commentService.findByPost(postId, PageRequest.of(0, 20)).getContent())
				.extracting(item -> item.id()).containsExactly(response.id());
	}

	@Test
	void suspiciousCommentIsHeldAndCanBeCorrectedByAuthor() {
		UUID postId = createPost("comment-held");
		authenticate(saveStudent("held-comment-author"));
		var held = commentService.create(postId, new CommentRequest("Isso e idiota"));

		assertThat(held.status()).isEqualTo(ContentStatus.HELD_FOR_REVIEW);
		assertThat(commentService.findByPost(postId, PageRequest.of(0, 20))).isEmpty();
		var persisted = commentRepository.findById(held.id()).orElseThrow();
		assertThat(persisted.getContentFilterRuleVersion()).isEqualTo("test-comment-filter-v1");

		var corrected = commentService.update(held.id(), new CommentRequest("Comentario respeitoso"));

		assertThat(corrected.status()).isEqualTo(ContentStatus.ACTIVE);
		assertThat(commentService.findByPost(postId, PageRequest.of(0, 20))).hasSize(1);
	}

	@Test
	void anotherUserCannotUpdateComment() {
		UUID postId = createPost("comment-owner");
		authenticate(saveStudent("original-comment-author"));
		var comment = commentService.create(postId, new CommentRequest("Original"));
		authenticate(saveStudent("other-comment-user"));

		assertThatThrownBy(() -> commentService.update(comment.id(), new CommentRequest("Changed")))
				.isInstanceOf(CommunityForbiddenException.class)
				.hasMessage("Only the comment author or an admin can manage this comment.");
	}

	@Test
	void removedCommentDisappearsFromPublicList() {
		UUID postId = createPost("comment-remove");
		authenticate(saveStudent("remove-comment-author"));
		var comment = commentService.create(postId, new CommentRequest("Remove me"));

		commentService.remove(comment.id(), "Removed by author");

		assertThat(commentService.findByPost(postId, PageRequest.of(0, 20))).isEmpty();
		var persisted = commentRepository.findById(comment.id()).orElseThrow();
		assertThat(persisted.getStatus()).isEqualTo(ContentStatus.REMOVED);
		assertThat(persisted.getRemovedAt()).isNotNull();
	}

	@Test
	void lockedPostRejectsNewComments() {
		UUID postId = createPost("comment-locked");
		postRepository.findById(postId).orElseThrow().lockComments();
		authenticate(saveStudent("locked-comment-author"));

		assertThatThrownBy(() -> commentService.create(postId, new CommentRequest("Blocked")))
				.isInstanceOf(CommentUnavailableException.class)
				.hasMessage("This post does not accept comments.");
	}

	@Test
	void fourthCommentWithinOneMinuteIsRateLimited() {
		UUID postId = createPost("comment-rate-limit");
		authenticate(saveStudent("limited-comment-author"));
		for (int index = 1; index <= 3; index++) {
			commentService.create(postId, new CommentRequest("Comment " + index));
		}

		assertThatThrownBy(() -> commentService.create(
				postId,
				new CommentRequest("Comment 4")
		))
				.isInstanceOf(CommunityRateLimitExceededException.class)
				.hasMessage("Rate limit exceeded for comments: maximum 3 per minute.");
	}

	@Test
	void normalizedDuplicateCommentIsRejectedWithinConfiguredWindow() {
		UUID postId = createPost("duplicate-comment");
		authenticate(saveStudent("duplicate-comment-author"));
		commentService.create(postId, new CommentRequest("Ótima explicação!"));

		assertThatThrownBy(() -> commentService.create(
				postId,
				new CommentRequest("  OTIMA   explicacao ")
		))
				.isInstanceOf(DuplicateContentException.class)
				.hasMessage("Duplicate comment content was recently submitted.");
	}

	private UUID createPost(String prefix) {
		User admin = saveAdmin(prefix + "-admin");
		authenticate(admin);
		UUID forumId = forumService.create(new ForumRequest(
				"Forum " + prefix,
				prefix + "-" + UUID.randomUUID(),
				"Forum description"
		)).id();
		return postService.create(forumId, new PostRequest(
				"Post " + prefix,
				"Post content",
				PostType.DISCUSSION,
				null,
				null,
				Set.of()
		)).id();
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
}
