package com.devnest.community;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devnest.auth.security.useridentity.CustomAuthentication;
import com.devnest.community.dto.forum.ForumRequest;
import com.devnest.community.dto.moderation.ModerationActionRequest;
import com.devnest.community.dto.post.PostRequest;
import com.devnest.community.dto.report.ReportRequest;
import com.devnest.community.dto.report.ReportReviewRequest;
import com.devnest.community.entity.moderation.ModerationActionType;
import com.devnest.community.entity.moderation.ModerationCaseStatus;
import com.devnest.community.entity.post.ContentStatus;
import com.devnest.community.entity.post.PostType;
import com.devnest.community.entity.report.ReportDecision;
import com.devnest.community.entity.report.ReportReason;
import com.devnest.community.exception.access.CommunityForbiddenException;
import com.devnest.community.exception.moderation.ModerationConflictException;
import com.devnest.community.repository.moderation.ModerationActionRepository;
import com.devnest.community.repository.moderation.ModerationCaseRepository;
import com.devnest.community.repository.post.PostRepository;
import com.devnest.community.service.forum.ForumService;
import com.devnest.community.service.moderation.ModerationService;
import com.devnest.community.service.post.PostService;
import com.devnest.community.service.report.ReportService;
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
class ModerationServiceTests {

	@Autowired private ModerationService moderationService;
	@Autowired private ReportService reportService;
	@Autowired private ForumService forumService;
	@Autowired private PostService postService;
	@Autowired private PostRepository postRepository;
	@Autowired private ModerationCaseRepository caseRepository;
	@Autowired private ModerationActionRepository actionRepository;
	@Autowired private UserRepository userRepository;

	@AfterEach
	void clearAuthentication() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void confirmedReportOpensCaseAndActionsAreAudited() {
		UUID postId = createPost("moderation-audit");
		authenticate(saveStudent("moderation-reporter"));
		var report = reportService.reportPost(
				postId,
				new ReportRequest(ReportReason.HARASSMENT, "Evidence")
		);
		User admin = saveAdmin("moderation-admin");
		authenticate(admin);
		reportService.review(report.id(), new ReportReviewRequest(ReportDecision.CONFIRM, "Confirmed"));

		var openCases = moderationService.findCases(ModerationCaseStatus.OPEN, PageRequest.of(0, 20));
		assertThat(openCases).hasSize(1);
		var moderationCase = openCases.getContent().getFirst();
		assertThat(moderationCase.reportId()).isEqualTo(report.id());
		assertThat(moderationCase.postId()).isEqualTo(postId);

		var action = moderationService.perform(
				moderationCase.id(),
				new ModerationActionRequest(ModerationActionType.HIDE, "Hide confirmed violation")
		);

		assertThat(postRepository.findById(postId).orElseThrow().getStatus()).isEqualTo(ContentStatus.HIDDEN);
		assertThat(action.moderatorId()).isEqualTo(admin.getId());
		assertThat(action.previousState()).contains("content=ACTIVE");
		assertThat(action.newState()).contains("content=HIDDEN");
		assertThat(actionRepository.count()).isEqualTo(1);
	}

	@Test
	void moderatorCanRestoreThenResolveCase() {
		var moderationCase = createConfirmedPostCase("moderation-resolve");

		moderationService.perform(moderationCase.id(),
				new ModerationActionRequest(ModerationActionType.REMOVE, "Remove content"));
		moderationService.perform(moderationCase.id(),
				new ModerationActionRequest(ModerationActionType.RESTORE, "Decision corrected"));
		var resolution = moderationService.perform(moderationCase.id(),
				new ModerationActionRequest(ModerationActionType.RESOLVE_CASE, "Review complete"));

		assertThat(resolution.newState()).contains("case=RESOLVED");
		assertThat(caseRepository.findById(moderationCase.id()).orElseThrow().getStatus())
				.isEqualTo(ModerationCaseStatus.RESOLVED);
		assertThat(actionRepository.findAllByModerationCaseIdOrderByPerformedAtAscIdAsc(moderationCase.id()))
				.hasSize(3);
		assertThatThrownBy(() -> moderationService.perform(moderationCase.id(),
				new ModerationActionRequest(ModerationActionType.HIDE, "Too late")))
				.isInstanceOf(ModerationConflictException.class)
				.hasMessage("Only open moderation cases accept actions.");
	}

	@Test
	void unsupportedActionDoesNotChangeCommentTarget() {
		// A post case proves the transition guard without relying on controller validation.
		var moderationCase = createConfirmedPostCase("moderation-invalid");
		moderationService.perform(moderationCase.id(),
				new ModerationActionRequest(ModerationActionType.REMOVE, "Remove once"));

		assertThatThrownBy(() -> moderationService.perform(moderationCase.id(),
				new ModerationActionRequest(ModerationActionType.REMOVE, "Remove twice")))
				.isInstanceOf(ModerationConflictException.class)
				.hasMessage("Post is already removed.");
		assertThat(actionRepository.findAllByModerationCaseIdOrderByPerformedAtAscIdAsc(moderationCase.id()))
				.hasSize(1);
	}

	@Test
	void nonAdminCannotReadModerationCases() {
		authenticate(saveStudent("moderation-reader"));

		assertThatThrownBy(() -> moderationService.findCases(null, PageRequest.of(0, 20)))
				.isInstanceOf(CommunityForbiddenException.class)
				.hasMessage("Only admins can manage community resources.");
	}

	private com.devnest.community.dto.moderation.ModerationCaseResponse createConfirmedPostCase(String prefix) {
		UUID postId = createPost(prefix);
		authenticate(saveStudent(prefix + "-reporter"));
		var report = reportService.reportPost(postId, new ReportRequest(ReportReason.SPAM, "Evidence"));
		authenticate(saveAdmin(prefix + "-moderator"));
		reportService.review(report.id(), new ReportReviewRequest(ReportDecision.CONFIRM, "Confirmed"));
		return moderationService.findCases(ModerationCaseStatus.OPEN, PageRequest.of(0, 20))
				.getContent().stream()
				.filter(item -> item.reportId().equals(report.id()))
				.findFirst().orElseThrow();
	}

	private UUID createPost(String prefix) {
		authenticate(saveAdmin(prefix + "-admin"));
		UUID forumId = forumService.create(new ForumRequest(
				"Forum " + prefix, prefix + "-" + UUID.randomUUID(), "Forum description"
		)).id();
		authenticate(saveStudent(prefix + "-author"));
		return postService.create(forumId, new PostRequest(
				"Post " + prefix, "Post content " + prefix, PostType.DISCUSSION,
				null, null, Set.of()
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
				principal, null, principal.getAuthorities()
		);
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	private String uniqueEmail(String prefix) {
		return prefix + "-" + UUID.randomUUID() + "@example.com";
	}
}
