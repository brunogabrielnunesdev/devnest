package com.devnest.community.repository.reaction;

import com.devnest.community.entity.reaction.Reaction;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository("communityReactionRepository")
public interface ReactionRepository extends JpaRepository<Reaction, UUID> {

	Optional<Reaction> findByUserIdAndPostId(UUID userId, UUID postId);

	Optional<Reaction> findByUserIdAndCommentId(UUID userId, UUID commentId);

	@Query("""
			select reaction.type as type, count(reaction) as total
			from CommunityReaction reaction
			where reaction.post.id = :postId
			group by reaction.type
			""")
	List<ReactionCount> countByPostGroupedByType(@Param("postId") UUID postId);

	@Query("""
			select reaction.type as type, count(reaction) as total
			from CommunityReaction reaction
			where reaction.comment.id = :commentId
			group by reaction.type
			""")
	List<ReactionCount> countByCommentGroupedByType(@Param("commentId") UUID commentId);
}
