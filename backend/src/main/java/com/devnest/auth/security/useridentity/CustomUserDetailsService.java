package com.devnest.auth.security.useridentity;

import com.devnest.identity.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) {
		return userRepository.findByEmail(username)
			.map(CustomAuthentication::new)
			.orElseThrow(() -> new UsernameNotFoundException("User not found."));
	}

	public CustomAuthentication loadUserById(UUID userId) {
		return userRepository.findById(userId)
			.map(CustomAuthentication::new)
			.orElseThrow(() -> new UsernameNotFoundException("User not found."));
	}
}
