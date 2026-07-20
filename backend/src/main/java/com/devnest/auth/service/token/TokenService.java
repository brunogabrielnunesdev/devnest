package com.devnest.auth.service.token;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.devnest.auth.security.useridentity.CustomAuthentication;
import com.devnest.identity.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class TokenService {

	private static final String ISSUER = "devnest-api";

	private static final long EXPIRATION_MINUTES = 3456L;
	private static final long EXPIRATION_MINUTES_ACCESS = 15L;

	private static final String TOKEN_TYPE_CLAIM = "token_type";
	private static final String TOKEN_TYPE_ACCESS = "access";
	private static final String TOKEN_TYPE_REFRESH = "refresh";

	private static final String USER_ID_CLAIM = "user_id";
	private static final String EMAIL_CLAIM = "email";
	private static final String ROLE_CLAIM = "role";
	private static final String TOKEN_VERSION_CLAIM = "token_version";

	@Value("${devnest.security.jwt-secret}")
	private String secret;

	public String generateAccessToken(User user) {
		Instant now = Instant.now();
		return JWT.create()
				.withIssuer(ISSUER)
				.withSubject(user.getId().toString())
				.withClaim(TOKEN_TYPE_CLAIM, TOKEN_TYPE_ACCESS)
				.withClaim(USER_ID_CLAIM, user.getId().toString())
				.withClaim(EMAIL_CLAIM, user.getEmail())
				.withClaim(ROLE_CLAIM, user.getRole().toString())
				.withClaim(TOKEN_VERSION_CLAIM, user.getTokenVersion())
				.withIssuedAt(now)
				.withExpiresAt(now.plusSeconds(EXPIRATION_MINUTES_ACCESS * 60))
				.sign(algorithm());
	}

	public String generateRefreshToken(User user) {
		Instant now = Instant.now();
		return JWT.create()
				.withIssuer(ISSUER)
				.withSubject(user.getId().toString())
				.withClaim(TOKEN_TYPE_CLAIM,TOKEN_TYPE_REFRESH)
				.withClaim(TOKEN_VERSION_CLAIM, user.getTokenVersion())
				.withIssuedAt(now)
				.withExpiresAt(now.plusSeconds(EXPIRATION_MINUTES * 60))
				.sign(algorithm());
	}

	public String getSubject(String token) {
		DecodedJWT jwt = verify(token);
		return jwt != null ? jwt.getSubject() : null;
	}

	public Integer getTokenVersion(String token) {
		DecodedJWT jwt = verify(token);
		return jwt != null ? jwt.getClaim(TOKEN_VERSION_CLAIM).asInt() : null;
	}

	public boolean isAccessToken(String token) {
		return hasTokenType(token, TOKEN_TYPE_ACCESS);
	}

	public boolean isRefreshToken(String token) {
		return hasTokenType(token, TOKEN_TYPE_REFRESH);
	}

	public boolean isTokenValid(String token, CustomAuthentication authentication) {
		String subject = getSubject(token);
		Integer tokenVersion = getTokenVersion(token);
		return subject != null
			&& tokenVersion != null
			&& subject.equals(authentication.getId().toString())
			&& tokenVersion.equals(authentication.user().getTokenVersion())
			&& authentication.isEnabled();
	}

	private boolean hasTokenType(String token, String expectedType) {
		try {
			DecodedJWT jwt = verify(token);
			return jwt != null && expectedType.equals(jwt.getClaim(TOKEN_TYPE_CLAIM).asString());
		} catch (JWTVerificationException exception) {
			return false;
		}
	}

	private DecodedJWT verify(String token) {
		try {
			return JWT.require(algorithm())
				.withIssuer(ISSUER)
				.build()
				.verify(token);
		} catch (JWTVerificationException exception) {
			return null;
		}
	}

	private Algorithm algorithm() {
		return Algorithm.HMAC256(secret);
	}
}
