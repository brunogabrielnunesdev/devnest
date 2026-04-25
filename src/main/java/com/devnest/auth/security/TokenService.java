package com.devnest.auth.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.devnest.user.domain.User;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

	private static final String ISSUER = "devnest-api";
	private static final long EXPIRATION_MINUTES = 120L;

	@Value("${JWT_SECRET:secret}")
	private String secret;

	public String generateToken(User user) {
		Instant now = Instant.now();
		return JWT.create()
			.withIssuer(ISSUER)
			.withSubject(user.getEmail())
			.withClaim("userId", user.getId().toString())
			.withClaim("role", user.getRole().name())
			.withIssuedAt(now)
			.withExpiresAt(now.plusSeconds(EXPIRATION_MINUTES * 60))
			.sign(algorithm());
	}

	public String getSubject(String token) {
		try {
			return JWT.require(algorithm())
				.withIssuer(ISSUER)
				.build()
				.verify(token)
				.getSubject();
		} catch (JWTVerificationException exception) {
			return null;
		}
	}

	public boolean isTokenValid(String token, UserDetails userDetails) {
		String subject = getSubject(token);
		return subject != null && subject.equals(userDetails.getUsername()) && userDetails.isEnabled();
	}

	private Algorithm algorithm() {
		return Algorithm.HMAC256(secret);
	}
}
