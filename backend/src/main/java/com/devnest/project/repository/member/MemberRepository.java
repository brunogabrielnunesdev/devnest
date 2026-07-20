package com.devnest.project.repository.member;

import com.devnest.project.entity.member.Member;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, UUID> {

	List<Member> findAllByProjectIdOrderByCreatedAtAsc(UUID projectId);

	Optional<Member> findByIdAndProjectId(UUID id, UUID projectId);

	Optional<Member> findByProjectIdAndUserId(UUID projectId, UUID userId);

	boolean existsByProjectIdAndUserId(UUID projectId, UUID userId);
}
