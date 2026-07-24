package com.devnest.community.entity.userrelation;

import com.devnest.common.entity.BaseEntity;
import com.devnest.identity.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "CommunityUserBlock")
@Getter
@Table(name = "community_user_blocks", indexes = {
		@Index(name = "idx_community_user_blocks_blocked", columnList = "blocked_user_id, blocker_id")
}, uniqueConstraints = @UniqueConstraint(
		name = "uk_community_user_blocks_pair",
		columnNames = {"blocker_id", "blocked_user_id"}
))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserBlock extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "blocker_id", nullable = false)
	private User blocker;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "blocked_user_id", nullable = false)
	private User blockedUser;

	public static UserBlock create(User blocker, User blockedUser) {
		UserBlock block = new UserBlock();
		block.blocker = blocker;
		block.blockedUser = blockedUser;
		return block;
	}
}
