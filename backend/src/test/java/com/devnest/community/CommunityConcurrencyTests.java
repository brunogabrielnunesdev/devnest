package com.devnest.community;

import static org.assertj.core.api.Assertions.assertThat;

import com.devnest.auth.security.useridentity.CustomAuthentication;
import com.devnest.community.dto.forum.ForumRequest;
import com.devnest.community.dto.post.PostRequest;
import com.devnest.community.dto.reaction.ReactionRequest;
import com.devnest.community.entity.post.PostType;
import com.devnest.community.entity.reaction.ReactionType;
import com.devnest.community.exception.post.PostLimitExceededException;
import com.devnest.community.repository.post.PostRepository;
import com.devnest.community.repository.reaction.ReactionRepository;
import com.devnest.community.service.forum.ForumService;
import com.devnest.community.service.post.PostService;
import com.devnest.community.service.reaction.ReactionService;
import com.devnest.identity.entity.User;
import com.devnest.identity.entity.UserRole;
import com.devnest.identity.repository.UserRepository;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(properties = {
		"devnest.community.limits.posts-per-24-hours=1",
		"devnest.community.limits.reactions-per-minute=10"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class CommunityConcurrencyTests {

	@Autowired private PostService postService;
	@Autowired private ReactionService reactionService;
	@Autowired private ForumService forumService;
	@Autowired private PostRepository postRepository;
	@Autowired private ReactionRepository reactionRepository;
	@Autowired private UserRepository userRepository;

	@Test
	void concurrentPostsCannotExceedAuthorLimit() throws Exception {
		UUID forumId = createForum("concurrent-posts");
		User author = saveStudent("concurrent-post-author");
		CyclicBarrier barrier = new CyclicBarrier(2);

		try (var executor = Executors.newFixedThreadPool(2)) {
			var first = executor.submit(() -> createPostConcurrently(
					barrier, author, forumId, "Concurrent post one"
			));
			var second = executor.submit(() -> createPostConcurrently(
					barrier, author, forumId, "Concurrent post two"
			));

			assertThat(Set.of(first.get(), second.get()))
					.containsExactlyInAnyOrder(true, false);
		}

		assertThat(postRepository.countByAuthorIdAndCreatedAtGreaterThanEqual(
				author.getId(),
				OffsetDateTime.now().minusHours(24)
		)).isEqualTo(1);
	}

	@Test
	void concurrentReactionChangesKeepSingleReaction() throws Exception {
		UUID postId = createPost("concurrent-reactions");
		User reactor = saveStudent("concurrent-reactor");
		CyclicBarrier barrier = new CyclicBarrier(2);

		try (var executor = Executors.newFixedThreadPool(2)) {
			var first = executor.submit(() -> {
				reactConcurrently(barrier, reactor, postId, ReactionType.LIKE);
				return null;
			});
			var second = executor.submit(() -> {
				reactConcurrently(barrier, reactor, postId, ReactionType.HELPFUL);
				return null;
			});
			first.get();
			second.get();
		}

		assertThat(reactionRepository.findByUserIdAndPostId(reactor.getId(), postId))
				.isPresent();
		assertThat(reactionRepository.count()).isEqualTo(1);
	}

	private boolean createPostConcurrently(
			CyclicBarrier barrier,
			User author,
			UUID forumId,
			String title
	) throws Exception {
		authenticate(author);
		barrier.await();
		try {
			postService.create(forumId, postRequest(title));
			return true;
		} catch (PostLimitExceededException exception) {
			return false;
		} finally {
			SecurityContextHolder.clearContext();
		}
	}

	private void reactConcurrently(
			CyclicBarrier barrier,
			User reactor,
			UUID postId,
			ReactionType type
	) throws Exception {
		authenticate(reactor);
		barrier.await();
		try {
			reactionService.reactToPost(postId, new ReactionRequest(type));
		} finally {
			SecurityContextHolder.clearContext();
		}
	}

	private UUID createForum(String prefix) {
		authenticate(saveAdmin(prefix + "-admin"));
		return forumService.create(new ForumRequest(
				"Forum " + prefix,
				prefix + "-" + UUID.randomUUID(),
				"Forum description"
		)).id();
	}

	private UUID createPost(String prefix) {
		UUID forumId = createForum(prefix);
		authenticate(saveAdmin(prefix + "-post-author"));
		return postService.create(forumId, postRequest("Post " + prefix)).id();
	}

	private PostRequest postRequest(String title) {
		return new PostRequest(
				title,
				"Post content " + title,
				PostType.DISCUSSION,
				null,
				null,
				Set.of()
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
}
