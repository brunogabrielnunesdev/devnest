package com.devnest.community.repository.report;

import com.devnest.community.entity.report.Report;
import com.devnest.community.entity.report.ReportStatus;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, UUID> {

	boolean existsByReporterIdAndPostId(UUID reporterId, UUID postId);

	boolean existsByReporterIdAndCommentId(UUID reporterId, UUID commentId);

	@EntityGraph(attributePaths = {"reporter", "post", "comment", "reviewedBy"})
	Page<Report> findAllByStatus(ReportStatus status, Pageable pageable);

	@Override
	@EntityGraph(attributePaths = {"reporter", "post", "comment", "reviewedBy"})
	Page<Report> findAll(Pageable pageable);
}
