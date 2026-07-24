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
import com.devnest.community.exception.access.CommunityForbiddenException;
import com.devnest.community.exception.userrelation.SelfRelationException;
import com.devnest.community.repository.userrelation.UserBlockRepository;
import com.devnest.community.repository.userrelation.UserMuteRepository;
import com.devnest.community.service.comment.CommentService;
import com.devnest.community.service.forum.ForumService;
import com.devnest.community.service.post.PostService;
import com.devnest.community.service.reaction.ReactionService;
import com.devnest.community.service.userrelation.UserRelationService;
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
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class UserRelationServiceTests {

	@Autowired private UserRelationService userRelationService;
	@Autowired private UserBlockRepository userBlockRepository;
	@Autowired private UserMuteRepository userMuteRepository;
	@Autowired private PostService postService;
	@Autowired private CommentService commentService;
	@Autowired private ReactionService reactionService;
	@Autowired private ForumService forumService;
	@Autowired private UserRepository userRepository;

	@AfterEach
	void clearAuthentication() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void blockAndUnblockAreIdempotentAndListed() {
		User current = saveStudent("blocker");
		User target = saveStudent("blocked");
		authenticate(current);

		userRelationService.block(target.getId());
		userRelationService.block(target.getId());

		assertThat(userBlockRepository.count()).isEqualTo(1);
		assertThat(userRelationService.findBlocked(PageRequest.of(0, 20)).getContent())
				.extracting(item -> item.userId())
				.containsExactly(target.getId());

		userRelationService.unblock(target.getId());
		userRelationService.unblock(target.getId());
		assertThat(userBlockRepository.count()).isZero();
	}

	@Test
	void usersCannotBlockOrMuteThemselves() {
		User current = saveStudent("self-relation");
		authenticate(current);

		assertThatThrownBy(() -> userRelationService.block(current.getId()))
				.isInstanceOf(SelfRelationException.class);
		assertThatThrownBy(() -> userRelationService.mute(current.getId()))
				.isInstanceOf(SelfRelationException.class);
	}

	@Test
	void blockPreventsCommentsAndReactionsInBothDirections() {
		UUID forumId = createForum("blocked-interaction");
		User first = saveStudent("first-block-user");
		User second = saveStudent("second-block-user");
		UUID firstPostId = createPost(forumId, first, "First post");
		UUID secondPostId = createPost(forumId, second, "Second post");
		authenticate(first);
		userRelationService.block(second.getId());

		assertThatThrownBy(() -> commentService.create(
				secondPostId,
				new CommentRequest("Blocked comment")
		)).isInstanceOf(CommunityForbiddenException.class);
		assertThatThrownBy(() -> reactionService.reactToPost(
				secondPostId,
				new ReactionRequest(ReactionType.LIKE)
		)).isInstanceOf(CommunityForbiddenException.class);

		authenticate(second);
		assertThatThrownBy(() -> commentService.create(
				firstPostId,
				new CommentRequest("Reverse blocked comment")
		)).isInstanceOf(CommunityForbiddenException.class);
		assertThatThrownBy(() -> reactionService.reactToPost(
				firstPostId,
				new ReactionRequest(ReactionType.HELPFUL)
		)).isInstanceOf(CommunityForbiddenException.class);
	}

	@Test
	void muteFiltersOnlyTheCurrentUsersFeedAndUnmuteRestoresIt() {
		UUID forumId = createForum("muted-feed");
		User author = saveStudent("muted-author");
		User viewer = saveStudent("muting-viewer");
		User otherViewer = saveStudent("other-viewer");
		UUID postId = createPost(forumId, author, "Muted post");
		authenticate(viewer);

		userRelationService.mute(author.getId());

		assertThat(userMuteRepository.count()).isEqualTo(1);
		assertThat(postService.findFeed(PageRequest.of(0, 20)).getContent())
				.noneMatch(post -> post.id().equals(postId));
		assertThat(postService.findForumFeed(forumId, PageRequest.of(0, 20)).getContent())
				.noneMatch(post -> post.id().equals(postId));

		authenticate(otherViewer);
		assertThat(postService.findFeed(PageRequest.of(0, 20)).getContent())
				.anyMatch(post -> post.id().equals(postId));

		authenticate(viewer);
		userRelationService.unmute(author.getId());
		assertThat(postService.findFeed(PageRequest.of(0, 20)).getContent())
				.anyMatch(post -> post.id().equals(postId));
	}

	@Test
	void muteIsIdempotentAndAppearsInMutedList() {
		User current = saveStudent("mute-owner");
		User target = saveStudent("muted-user");
		authenticate(current);

		userRelationService.mute(target.getId());
		userRelationService.mute(target.getId());

		assertThat(userMuteRepository.count()).isEqualTo(1);
		assertThat(userRelationService.findMuted(PageRequest.of(0, 20)).getContent())
				.extracting(item -> item.userId())
				.containsExactly(target.getId());
	}

	private UUID createForum(String prefix) {
		User admin = saveAdmin(prefix + "-admin");
		authenticate(admin);
		return forumService.create(new ForumRequest(
				"Forum " + prefix,
				prefix + "-" + UUID.randomUUID(),
				"Forum description"
		)).id();
	}

	private UUID createPost(UUID forumId, User author, String title) {
		authenticate(author);
		return postService.create(forumId, new PostRequest(
				title,
				"Post content",
				PostType.DISCUSSION,
				null,
				null,
				Set.of()
		)).id();
	}

	private User saveStudent(String prefix) {
		return userRepository.save(User.createStudent(uniqueEmail(prefix), "password-hash", prefix));
	}

	private User saveAdmin(String prefix) {
		User admin = User.createStudent(uniqueEmail(prefix), "password-hash", prefix);
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
