package com.devnest.auth.security;

import com.devnest.user.domain.User;
import com.devnest.user.domain.UserStatus;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public record AuthenticatedUser(User user) implements UserDetails {

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
	}

	@Override
	public String getPassword() {
		return user.getPasswordHash();
	}

	@Override
	public String getUsername() {
		return user.getEmail();
	}

	@Override
	public boolean isEnabled() {
		return user.getStatus() == UserStatus.ACTIVE;
	}

	public UUID getId() {
		return user.getId();
	}
}
