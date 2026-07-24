package com.devnest.community.repository.userrelation;

import com.devnest.community.entity.userrelation.UserBlock;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserBlockRepository extends JpaRepository<UserBlock, UUID> {

	Optional<UserBlock> findByBlockerIdAndBlockedUserId(UUID blockerId, UUID blockedUserId);

	Page<UserBlock> findAllByBlockerId(UUID blockerId, Pageable pageable);

	@Query("""
			select count(block) > 0
			from CommunityUserBlock block
			where (block.blocker.id = :firstId and block.blockedUser.id = :secondId)
			   or (block.blocker.id = :secondId and block.blockedUser.id = :firstId)
			""")
	boolean existsBetween(@Param("firstId") UUID firstId, @Param("secondId") UUID secondId);
}
