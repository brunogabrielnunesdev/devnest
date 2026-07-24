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

@Entity(name = "CommunityUserMute")
@Getter
@Table(name = "community_user_mutes", indexes = {
		@Index(name = "idx_community_user_mutes_muted", columnList = "muted_user_id, user_id")
}, uniqueConstraints = @UniqueConstraint(
		name = "uk_community_user_mutes_pair",
		columnNames = {"user_id", "muted_user_id"}
))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserMute extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "muted_user_id", nullable = false)
	private User mutedUser;

	public static UserMute create(User user, User mutedUser) {
		UserMute mute = new UserMute();
		mute.user = user;
		mute.mutedUser = mutedUser;
		return mute;
	}
}
