package com.devnest.community.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "devnest.community.limits")
public class CommunityLimitsProperties {

	@Min(1)
	@Max(100)
	private int postsPer24Hours = 5;

	@Min(1)
	@Max(1000)
	private int commentsPerMinute = 20;

	@Min(1)
	@Max(1000)
	private int reactionsPerMinute = 60;

	@Min(1)
	@Max(1440)
	private int duplicateContentWindowMinutes = 10;

	public int getPostsPer24Hours() {
		return postsPer24Hours;
	}

	public void setPostsPer24Hours(int postsPer24Hours) {
		this.postsPer24Hours = postsPer24Hours;
	}

	public int getCommentsPerMinute() {
		return commentsPerMinute;
	}

	public void setCommentsPerMinute(int commentsPerMinute) {
		this.commentsPerMinute = commentsPerMinute;
	}

	public int getReactionsPerMinute() {
		return reactionsPerMinute;
	}

	public void setReactionsPerMinute(int reactionsPerMinute) {
		this.reactionsPerMinute = reactionsPerMinute;
	}

	public int getDuplicateContentWindowMinutes() {
		return duplicateContentWindowMinutes;
	}

	public void setDuplicateContentWindowMinutes(int duplicateContentWindowMinutes) {
		this.duplicateContentWindowMinutes = duplicateContentWindowMinutes;
	}
}
