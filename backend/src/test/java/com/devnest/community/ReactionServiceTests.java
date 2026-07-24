package com.devnest.community;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devnest.auth.security.useridentity.CustomAuthentication;
import com.devnest.community.dto.comment.CommentRequest;
import com.devnest.community.dto.forum.ForumRequest;
import com.devnest.community.dto.post.PostRequest;
import com.devnest.community.dto.reaction.ReactionRequest;
import com.devnest.community.entity.post.PostType;
import com.devnest.community.entity.reaction.ReactionType;
import com.devnest.community.exception.comment.CommentNotFoundException;
import com.devnest.community.exception.post.PostNotFoundException;
import com.devnest.community.repository.reaction.ReactionRepository;
import com.devnest.community.service.comment.CommentService;
import com.devnest.community.service.forum.ForumService;
import com.devnest.community.service.post.PostService;
import com.devnest.community.service.reaction.ReactionService;
import com.devnest.identity.entity.User;
import com.devnest.identity.entity.UserRole;
import com.devnest.identity.repository.UserRepository;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@TestPropertySource(properties = {
		"devnest.community.content-filter.rule-version=test-reaction-filter-v1",
		"devnest.community.content-filter.bad-words[0]=idiota"
})
class ReactionServiceTests {

	@Autowired private ReactionService reactionService;
	@Autowired private ReactionRepository reactionRepository;
	@Autowired private CommentService commentService;
	@Autowired private PostService postService;
	@Autowired private ForumService forumService;
	@Autowired private UserRepository userRepository;

	@AfterEach
	void clearAuthentication() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void userReactsToPostAndChangesTypeWithoutDuplication() {
		UUID postId = createPost("post-reaction");
		User user = saveStudent("post-reactor");
		authenticate(user);

		var first = reactionService.reactToPost(postId, new ReactionRequest(ReactionType.LIKE));
		var changed = reactionService.reactToPost(postId, new ReactionRequest(ReactionType.INSIGHTFUL));

		assertThat(changed.id()).isEqualTo(first.id());
		assertThat(changed.type()).isEqualTo(ReactionType.INSIGHTFUL);
		assertThat(reactionRepository.count()).isEqualTo(1);
		var summary = reactionService.summarizePost(postId);
		assertThat(summary.total()).isEqualTo(1);
		assertThat(summary.counts().get(ReactionType.LIKE)).isZero();
		assertThat(summary.counts().get(ReactionType.INSIGHTFUL)).isEqualTo(1);
		assertThat(summary.currentUserReaction()).isEqualTo(ReactionType.INSIGHTFUL);
	}

	@Test
	void postSummaryAggregatesReactionsFromDifferentUsers() {
		UUID postId = createPost("post-summary");
		authenticate(saveStudent("first-reactor"));
		reactionService.reactToPost(postId, new ReactionRequest(ReactionType.HELPFUL));
		authenticate(saveStudent("second-reactor"));
		reactionService.reactToPost(postId, new ReactionRequest(ReactionType.HELPFUL));
		authenticate(saveStudent("third-reactor"));
		reactionService.reactToPost(postId, new ReactionRequest(ReactionType.CELEBRATE));

		var summary = reactionService.summarizePost(postId);

		assertThat(summary.total()).isEqualTo(3);
		assertThat(summary.counts().get(ReactionType.HELPFUL)).isEqualTo(2);
		assertThat(summary.counts().get(ReactionType.CELEBRATE)).isEqualTo(1);
	}

	@Test
	void removingPostReactionIsIdempotent() {
		UUID postId = createPost("remove-reaction");
		authenticate(saveStudent("remove-reactor"));
		reactionService.reactToPost(postId, new ReactionRequest(ReactionType.LIKE));

		reactionService.removeFromPost(postId);
		reactionService.removeFromPost(postId);

		assertThat(reactionRepository.count()).isZero();
		assertThat(reactionService.summarizePost(postId).total()).isZero();
	}

	@Test
	void userReactsToActiveComment() {
		UUID postId = createPost("comment-reaction");
		authenticate(saveStudent("comment-author"));
		var comment = commentService.create(postId, new CommentRequest("Useful answer"));
		User reactor = saveStudent("comment-reactor");
		authenticate(reactor);

		var response = reactionService.reactToComment(
				comment.id(),
				new ReactionRequest(ReactionType.HELPFUL)
		);

		assertThat(response.commentId()).isEqualTo(comment.id());
		assertThat(response.postId()).isNull();
		assertThat(reactionService.summarizeComment(comment.id()).currentUserReaction())
				.isEqualTo(ReactionType.HELPFUL);
	}

	@Test
	void removedPostAndCommentRejectReactions() {
		UUID postId = createPost("unavailable-reaction");
		User author = saveStudent("unavailable-author");
		authenticate(author);
		var comment = commentService.create(postId, new CommentRequest("Temporary comment"));
		commentService.remove(comment.id(), "Removed");

		assertThatThrownBy(() -> reactionService.reactToComment(
				comment.id(),
				new ReactionRequest(ReactionType.LIKE)
		)).isInstanceOf(CommentNotFoundException.class);

		authenticate(saveAdmin("remove-post-admin"));
		postService.remove(postId, "Removed");
		assertThatThrownBy(() -> reactionService.reactToPost(
				postId,
				new ReactionRequest(ReactionType.LIKE)
		)).isInstanceOf(PostNotFoundException.class);
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
