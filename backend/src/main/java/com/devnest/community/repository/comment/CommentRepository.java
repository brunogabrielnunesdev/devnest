package com.devnest.community.repository.comment;

import com.devnest.community.entity.comment.Comment;
import com.devnest.community.entity.post.ContentStatus;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("communityCommentRepository")
public interface CommentRepository extends JpaRepository<Comment, UUID> {

	Optional<Comment> findByIdAndStatus(UUID id, ContentStatus status);

	Page<Comment> findAllByPostIdAndStatus(UUID postId, ContentStatus status, Pageable pageable);

	List<Comment> findAllByAuthorIdAndCreatedAtGreaterThanEqual(
			UUID authorId,
			OffsetDateTime createdAfter
	);
}
