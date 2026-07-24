package com.devnest.community.repository.forum;

import com.devnest.community.entity.forum.Forum;
import com.devnest.community.entity.forum.ForumStatus;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ForumRepository extends JpaRepository<Forum, UUID> {

	Optional<Forum> findBySlug(String slug);

	Optional<Forum> findBySlugAndStatus(String slug, ForumStatus status);

	Page<Forum> findAllByStatus(ForumStatus status, Pageable pageable);

	boolean existsBySlug(String slug);
}
