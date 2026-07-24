package com.devnest.community;

import static org.assertj.core.api.Assertions.assertThat;

import com.devnest.community.config.CommunityLimitsProperties;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"devnest.community.limits.posts-per-24-hours=7",
		"devnest.community.limits.comments-per-minute=25",
		"devnest.community.limits.reactions-per-minute=70",
		"devnest.community.limits.duplicate-content-window-minutes=15"
})
class CommunityLimitsPropertiesTests {

	@Autowired
	private CommunityLimitsProperties limits;

	@Autowired
	private Validator validator;

	@Test
	void bindsConfiguredCommunityLimits() {
		assertThat(limits.getPostsPer24Hours()).isEqualTo(7);
		assertThat(limits.getCommentsPerMinute()).isEqualTo(25);
		assertThat(limits.getReactionsPerMinute()).isEqualTo(70);
		assertThat(limits.getDuplicateContentWindowMinutes()).isEqualTo(15);
	}

	@Test
	void rejectsNonPositiveLimits() {
		CommunityLimitsProperties invalid = new CommunityLimitsProperties();
		invalid.setPostsPer24Hours(0);
		invalid.setCommentsPerMinute(0);
		invalid.setReactionsPerMinute(0);
		invalid.setDuplicateContentWindowMinutes(0);

		assertThat(validator.validate(invalid))
				.extracting(violation -> violation.getPropertyPath().toString())
				.containsExactlyInAnyOrder(
						"postsPer24Hours",
						"commentsPerMinute",
						"reactionsPerMinute",
						"duplicateContentWindowMinutes"
				);
	}
}
