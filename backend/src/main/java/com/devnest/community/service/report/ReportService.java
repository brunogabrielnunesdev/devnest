package com.devnest.community.service.report;

import com.devnest.community.dto.report.ReportRequest;
import com.devnest.community.dto.report.ReportResponse;
import com.devnest.community.dto.report.ReportReviewRequest;
import com.devnest.community.entity.comment.Comment;
import com.devnest.community.entity.post.ContentStatus;
import com.devnest.community.entity.post.Post;
import com.devnest.community.entity.report.Report;
import com.devnest.community.entity.report.ReportStatus;
import com.devnest.community.entity.report.ReportDecision;
import com.devnest.community.exception.comment.CommentNotFoundException;
import com.devnest.community.exception.post.PostNotFoundException;
import com.devnest.community.exception.report.ReportConflictException;
import com.devnest.community.exception.report.ReportNotFoundException;
import com.devnest.community.mapper.report.ReportMapper;
import com.devnest.community.repository.comment.CommentRepository;
import com.devnest.community.repository.post.PostRepository;
import com.devnest.community.repository.report.ReportRepository;
import com.devnest.community.service.access.AccessService;
import com.devnest.community.service.moderation.ModerationService;
import com.devnest.identity.entity.User;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {

	private final AccessService accessService;
	private final PostRepository postRepository;
	private final CommentRepository commentRepository;
	private final ReportRepository reportRepository;
	private final ReportMapper reportMapper;
	private final ModerationService moderationService;
	private final Clock communityClock;

	@Transactional
	public ReportResponse reportPost(UUID postId, ReportRequest request) {
		User reporter = accessService.getAuthenticatedUser();
		Post post = postRepository.findByIdAndStatus(postId, ContentStatus.ACTIVE)
				.orElseThrow(PostNotFoundException::new);
		validateDifferentAuthor(reporter, post.getAuthor());
		if (reportRepository.existsByReporterIdAndPostId(reporter.getId(), postId)) {
			throw duplicateReport();
		}
		return save(Report.forPost(reporter, post, request.reason(), request.description()));
	}

	@Transactional
	public ReportResponse reportComment(UUID commentId, ReportRequest request) {
		User reporter = accessService.getAuthenticatedUser();
		Comment comment = commentRepository.findByIdAndStatus(commentId, ContentStatus.ACTIVE)
				.orElseThrow(CommentNotFoundException::new);
		validateDifferentAuthor(reporter, comment.getAuthor());
		if (reportRepository.existsByReporterIdAndCommentId(reporter.getId(), commentId)) {
			throw duplicateReport();
		}
		return save(Report.forComment(reporter, comment, request.reason(), request.description()));
	}

	@Transactional(readOnly = true)
	public Page<ReportResponse> findQueue(ReportStatus status, Pageable pageable) {
		accessService.getAuthenticatedModerator();
		Page<Report> reports = status == null
				? reportRepository.findAll(pageable)
				: reportRepository.findAllByStatus(status, pageable);
		return reports.map(reportMapper::toResponse);
	}

	@Transactional
	public ReportResponse review(UUID reportId, ReportReviewRequest request) {
		User admin = accessService.getAuthenticatedModerator();
		Report report = reportRepository.findById(reportId)
				.orElseThrow(ReportNotFoundException::new);
		if (report.getStatus() != ReportStatus.PENDING) {
			throw new ReportConflictException("Only pending reports can be reviewed.");
		}
		OffsetDateTime reviewedAt = OffsetDateTime.now(communityClock);
		report.review(
				request.decision(),
				admin,
				request.note(),
				reviewedAt
		);
		if (request.decision() == ReportDecision.CONFIRM) {
			moderationService.openCase(report, admin, reviewedAt);
		}
		return reportMapper.toResponse(report);
	}

	private ReportResponse save(Report report) {
		try {
			return reportMapper.toResponse(reportRepository.saveAndFlush(report));
		} catch (DataIntegrityViolationException exception) {
			throw duplicateReport();
		}
	}

	private void validateDifferentAuthor(User reporter, User author) {
		if (reporter.getId().equals(author.getId())) {
			throw new ReportConflictException("Users cannot report their own content.");
		}
	}

	private ReportConflictException duplicateReport() {
		return new ReportConflictException("This content has already been reported by the user.");
	}
}
