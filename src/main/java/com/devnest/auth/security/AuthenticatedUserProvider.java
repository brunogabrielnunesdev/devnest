package com.devnest.auth.security;

import com.devnest.shared.exception.UnauthorizedException;
import com.devnest.user.domain.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticatedUserProvider {

	public User getAuthenticatedUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser)) {
			throw new UnauthorizedException("User is not authenticated.");
		}

		return authenticatedUser.user();
	}
}
