package com.devnest.community.repository.moderation;

import com.devnest.community.entity.moderation.ModerationCase;
import com.devnest.community.entity.moderation.ModerationCaseStatus;
import java.util.UUID;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface ModerationCaseRepository extends JpaRepository<ModerationCase, UUID> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select moderationCase from CommunityModerationCase moderationCase where moderationCase.id = :id")
	Optional<ModerationCase> findByIdForUpdate(UUID id);

	@EntityGraph(attributePaths = {"report", "post", "comment", "openedBy", "resolvedBy"})
	Page<ModerationCase> findAllByStatus(ModerationCaseStatus status, Pageable pageable);

	@EntityGraph(attributePaths = {"report", "post", "comment", "openedBy", "resolvedBy"})
	Page<ModerationCase> findAll(Pageable pageable);
}
