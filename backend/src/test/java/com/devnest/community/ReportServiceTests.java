package com.devnest.community;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devnest.auth.security.useridentity.CustomAuthentication;
import com.devnest.community.dto.comment.CommentRequest;
import com.devnest.community.dto.forum.ForumRequest;
import com.devnest.community.dto.post.PostRequest;
import com.devnest.community.dto.report.ReportRequest;
import com.devnest.community.dto.report.ReportReviewRequest;
import com.devnest.community.entity.post.PostType;
import com.devnest.community.entity.report.ReportDecision;
import com.devnest.community.entity.report.ReportReason;
import com.devnest.community.entity.report.ReportStatus;
import com.devnest.community.exception.access.CommunityForbiddenException;
import com.devnest.community.exception.report.ReportConflictException;
import com.devnest.community.repository.report.ReportRepository;
import com.devnest.community.service.comment.CommentService;
import com.devnest.community.service.forum.ForumService;
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
class ReportServiceTests {

	@Autowired private ReportService reportService;
	@Autowired private ReportRepository reportRepository;
	@Autowired private PostService postService;
	@Autowired private CommentService commentService;
	@Autowired private ForumService forumService;
	@Autowired private UserRepository userRepository;

	@AfterEach
	void clearAuthentication() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void userReportsPostOnlyOnce() {
		UUID postId = createPost("report-post");
		User reporter = saveStudent("post-reporter");
		authenticate(reporter);

		var response = reportService.reportPost(
				postId,
				new ReportRequest(ReportReason.SPAM, "Repeated promotion")
		);

		assertThat(response.reporterId()).isEqualTo(reporter.getId());
		assertThat(response.postId()).isEqualTo(postId);
		assertThat(response.commentId()).isNull();
		assertThat(response.status()).isEqualTo(ReportStatus.PENDING);
		assertThatThrownBy(() -> reportService.reportPost(
				postId,
				new ReportRequest(ReportReason.OTHER, "Again")
		))
				.isInstanceOf(ReportConflictException.class)
				.hasMessage("This content has already been reported by the user.");
	}

	@Test
	void userReportsCommentAndCannotReportOwnContent() {
		UUID postId = createPost("report-comment");
		User commentAuthor = saveStudent("reported-comment-author");
		authenticate(commentAuthor);
		var comment = commentService.create(postId, new CommentRequest("Reported comment"));
		User reporter = saveStudent("comment-reporter");
		authenticate(reporter);

		var response = reportService.reportComment(
				comment.id(),
				new ReportRequest(ReportReason.HARASSMENT, null)
		);
		assertThat(response.commentId()).isEqualTo(comment.id());

		authenticate(commentAuthor);
		assertThatThrownBy(() -> reportService.reportComment(
				comment.id(),
				new ReportRequest(ReportReason.OTHER, "Own content")
		))
				.isInstanceOf(ReportConflictException.class)
				.hasMessage("Users cannot report their own content.");
	}

	@Test
	void adminFiltersQueueAndReviewsPendingReport() {
		UUID postId = createPost("review-report");
		authenticate(saveStudent("review-reporter"));
		var pending = reportService.reportPost(
				postId,
				new ReportRequest(ReportReason.MISINFORMATION, "Incorrect information")
		);
		User admin = saveAdmin("report-admin");
		authenticate(admin);

		var queue = reportService.findQueue(
				ReportStatus.PENDING,
				PageRequest.of(0, 20)
		);
		assertThat(queue.getContent()).extracting(item -> item.id()).contains(pending.id());

		var reviewed = reportService.review(
				pending.id(),
				new ReportReviewRequest(ReportDecision.CONFIRM, "Violation confirmed")
		);
		assertThat(reviewed.status()).isEqualTo(ReportStatus.CONFIRMED);
		assertThat(reviewed.reviewedById()).isEqualTo(admin.getId());
		assertThat(reviewed.reviewedAt()).isNotNull();
		assertThat(reviewed.reviewNote()).isEqualTo("Violation confirmed");
		assertThat(reportRepository.findById(pending.id()).orElseThrow().getStatus())
				.isEqualTo(ReportStatus.CONFIRMED);

		assertThatThrownBy(() -> reportService.review(
				pending.id(),
				new ReportReviewRequest(ReportDecision.DISMISS, "Second decision")
		))
				.isInstanceOf(ReportConflictException.class)
				.hasMessage("Only pending reports can be reviewed.");
	}

	@Test
	void nonAdminCannotReadReportQueue() {
		authenticate(saveStudent("queue-reader"));

		assertThatThrownBy(() -> reportService.findQueue(
				null,
				PageRequest.of(0, 20)
		))
				.isInstanceOf(CommunityForbiddenException.class)
				.hasMessage("Only admins can manage community resources.");
	}

	private UUID createPost(String prefix) {
		authenticate(saveAdmin(prefix + "-admin"));
		UUID forumId = forumService.create(new ForumRequest(
				"Forum " + prefix,
				prefix + "-" + UUID.randomUUID(),
				"Forum description"
		)).id();
		authenticate(saveStudent(prefix + "-author"));
		return postService.create(forumId, new PostRequest(
				"Post " + prefix,
				"Post content " + prefix,
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
