package com.devnest.community.repository.post;

import com.devnest.community.entity.post.ContentStatus;
import com.devnest.community.entity.post.Post;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, UUID> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select post from Post post where post.id = :id")
	Optional<Post> findByIdForUpdate(@Param("id") UUID id);

	Optional<Post> findByIdAndStatus(UUID id, ContentStatus status);

	boolean existsByIdAndStatus(UUID id, ContentStatus status);

	Page<Post> findAllByStatus(
			ContentStatus status,
			Pageable pageable
	);

	@Query(value = """
			select post
			from Post post
			where post.status = :status
			  and not exists (
			      select mute.id
			      from CommunityUserMute mute
			      where mute.user.id = :viewerId
			        and mute.mutedUser.id = post.author.id
			  )
			""", countQuery = """
			select count(post)
			from Post post
			where post.status = :status
			  and not exists (
			      select mute.id
			      from CommunityUserMute mute
			      where mute.user.id = :viewerId
			        and mute.mutedUser.id = post.author.id
			  )
			""")
	Page<Post> findFeedForUser(
			@Param("viewerId") UUID viewerId,
			@Param("status") ContentStatus status,
			Pageable pageable
	);

	Page<Post> findAllByForumIdAndStatus(
			UUID forumId,
			ContentStatus status,
			Pageable pageable
	);

	@Query(value = """
			select post
			from Post post
			where post.forum.id = :forumId
			  and post.status = :status
			  and not exists (
			      select mute.id
			      from CommunityUserMute mute
			      where mute.user.id = :viewerId
			        and mute.mutedUser.id = post.author.id
			  )
			""", countQuery = """
			select count(post)
			from Post post
			where post.forum.id = :forumId
			  and post.status = :status
			  and not exists (
			      select mute.id
			      from CommunityUserMute mute
			      where mute.user.id = :viewerId
			        and mute.mutedUser.id = post.author.id
			  )
			""")
	Page<Post> findForumFeedForUser(
			@Param("forumId") UUID forumId,
			@Param("viewerId") UUID viewerId,
			@Param("status") ContentStatus status,
			Pageable pageable
	);

	long countByAuthorIdAndCreatedAtGreaterThanEqual(
			UUID authorId,
			OffsetDateTime createdAfter
	);

	List<Post> findAllByAuthorIdAndCreatedAtGreaterThanEqual(
			UUID authorId,
			OffsetDateTime createdAfter
	);
}
