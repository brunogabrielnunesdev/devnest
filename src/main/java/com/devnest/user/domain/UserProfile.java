package com.devnest.user.domain;

import com.devnest.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "user_profiles")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProfile extends BaseEntity {

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false, unique = true)
	private User user;

	@Column(name = "display_name", nullable = false, length = 80)
	private String displayName;

	@Column(name = "full_name", length = 120)
	private String fullName;

	@Column(columnDefinition = "text")
	private String bio;

	@Column(name = "avatar_url")
	private String avatarUrl;

	@Column(name = "github_url")
	private String githubUrl;

	@Column(name = "linkedin_url")
	private String linkedinUrl;

	@Column(name = "portfolio_url")
	private String portfolioUrl;

	@Column(length = 120)
	private String location;

	static UserProfile create(User user, String displayName) {
		UserProfile profile = new UserProfile();
		profile.user = user;
		profile.displayName = displayName;
		return profile;
	}
}
