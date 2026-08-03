package com.devnest.community.repository.moderation;

import com.devnest.community.entity.moderation.ModerationAction;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModerationActionRepository extends JpaRepository<ModerationAction, UUID> {

	@EntityGraph(attributePaths = {"moderator"})
	List<ModerationAction> findAllByModerationCaseIdOrderByPerformedAtAscIdAsc(UUID caseId);
}
