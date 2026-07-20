package com.devnest.auth.security.useridentity;

import com.devnest.common.exception.UnauthorizedException;
import com.devnest.identity.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CustomUserProvider {

	public User getAuthenticatedUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !(authentication.getPrincipal() instanceof CustomAuthentication customAuthentication)) {
			throw new UnauthorizedException("User is not authenticated.");
		}

		return customAuthentication.user();
	}
}
