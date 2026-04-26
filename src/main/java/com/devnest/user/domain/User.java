package com.devnest.user.domain;

import com.devnest.shared.domain.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

	@Column(nullable = false, unique = true, length = 320)
	private String email;

	@Column(name = "password_hash", nullable = false)
	private String passwordHash;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private UserRole role;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private UserStatus status;

	@OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	@Setter(AccessLevel.PACKAGE)
	private UserProfile profile;

	public static User createStudent(String email, String passwordHash, String displayName) {
		return create(email, passwordHash, displayName, UserRole.STUDENT);
	}

	public static User createTeacher(String email, String passwordHash, String displayName) {
		return create(email, passwordHash, displayName, UserRole.TEACHER);
	}

	private static User create(String email, String passwordHash, String displayName, UserRole role) {
		User user = new User();
		user.email = email;
		user.passwordHash = passwordHash;
		user.role = role;
		user.status = UserStatus.ACTIVE;
		user.profile = UserProfile.create(user, displayName);
		return user;
	}
}
