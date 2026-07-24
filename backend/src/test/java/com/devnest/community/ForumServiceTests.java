package com.devnest.community;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devnest.auth.security.useridentity.CustomAuthentication;
import com.devnest.community.dto.forum.ForumRequest;
import com.devnest.community.entity.forum.ForumStatus;
import com.devnest.community.exception.access.CommunityForbiddenException;
import com.devnest.community.exception.forum.ForumNotFoundException;
import com.devnest.community.exception.slug.SlugConflictException;
import com.devnest.community.repository.forum.ForumRepository;
import com.devnest.community.service.forum.ForumService;
import com.devnest.identity.entity.User;
import com.devnest.identity.entity.UserRole;
import com.devnest.identity.repository.UserRepository;
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
class ForumServiceTests {

	@Autowired
	private ForumService forumService;

	@Autowired
	private ForumRepository forumRepository;

	@Autowired
	private UserRepository userRepository;

	@AfterEach
	void clearAuthentication() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void adminCreatesActiveForum() {
		User admin = saveAdmin(uniqueEmail("admin-create"));
		authenticate(admin);

		var response = forumService.create(request("Java", "java", "Java discussions"));

		assertThat(response.id()).isNotNull();
		assertThat(response.createdById()).isEqualTo(admin.getId());
		assertThat(response.status()).isEqualTo(ForumStatus.ACTIVE);
		assertThat(forumRepository.findById(response.id())).isPresent();
	}

	@Test
	void nonAdminCannotCreateForum() {
		User student = userRepository.save(User.createStudent(
				uniqueEmail("student-create"), "password-hash", "Student"
		));
		authenticate(student);

		assertThatThrownBy(() -> forumService.create(request("Java", "java-student", "Description")))
				.isInstanceOf(CommunityForbiddenException.class)
				.hasMessage("Only admins can manage community forums.");
	}

	@Test
	void duplicateSlugIsRejected() {
		User admin = saveAdmin(uniqueEmail("admin-duplicate"));
		authenticate(admin);
		forumService.create(request("Java", "duplicate-slug", "First forum"));

		assertThatThrownBy(() -> forumService.create(
				request("Another Java", "duplicate-slug", "Second forum")
		))
				.isInstanceOf(SlugConflictException.class)
				.hasMessage("Community forum slug is already in use.");
	}

	@Test
	void archivedForumIsExcludedFromPublicQueriesAndCanBeRestored() {
		User admin = saveAdmin(uniqueEmail("admin-archive"));
		authenticate(admin);
		var created = forumService.create(request("Spring", "spring-archive", "Spring discussions"));

		var archived = forumService.archive(created.id());

		assertThat(archived.status()).isEqualTo(ForumStatus.ARCHIVED);
		assertThat(forumService.findActive(PageRequest.of(0, 20)).getContent())
				.noneMatch(forum -> forum.id().equals(created.id()));
		assertThatThrownBy(() -> forumService.findActiveBySlug("spring-archive"))
				.isInstanceOf(ForumNotFoundException.class);

		var restored = forumService.restore(created.id());
		assertThat(restored.status()).isEqualTo(ForumStatus.ACTIVE);
		assertThat(forumService.findActiveBySlug("spring-archive").id()).isEqualTo(created.id());
	}

	@Test
	void adminUpdatesForumKeepingItsCurrentSlug() {
		User admin = saveAdmin(uniqueEmail("admin-update"));
		authenticate(admin);
		var created = forumService.create(request("Old name", "same-slug", "Old description"));

		var updated = forumService.update(
				created.id(),
				request("New name", "same-slug", "New description")
		);

		assertThat(updated.name()).isEqualTo("New name");
		assertThat(updated.slug()).isEqualTo("same-slug");
		assertThat(updated.description()).isEqualTo("New description");
	}

	@Test
	void updatingMissingForumReturnsNotFound() {
		User admin = saveAdmin(uniqueEmail("admin-missing"));
		authenticate(admin);

		assertThatThrownBy(() -> forumService.update(
				UUID.randomUUID(),
				request("Missing", "missing", "Description")
		))
				.isInstanceOf(ForumNotFoundException.class)
				.hasMessage("Community forum not found.");
	}

	private ForumRequest request(String name, String slug, String description) {
		return new ForumRequest(name, slug, description);
	}

	private User saveAdmin(String email) {
		User admin = User.createStudent(email, "password-hash", "Admin");
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
