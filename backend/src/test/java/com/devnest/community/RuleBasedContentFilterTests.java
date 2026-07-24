package com.devnest.community;

import static org.assertj.core.api.Assertions.assertThat;

import com.devnest.community.config.ContentFilterProperties;
import com.devnest.community.service.content.ContentFilterDecision;
import com.devnest.community.service.content.ContentFilterResult;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RuleBasedContentFilterTests {

	private ContentFilterResult.RuleBasedContentFilter filter;

	@BeforeEach
	void setUp() {
		ContentFilterProperties properties = new ContentFilterProperties();
		properties.setRuleVersion("test-v1");
		properties.setBadWords(List.of("porra", "idiota"));
		properties.setExceptions(List.of("porradaria"));
		filter = new ContentFilterResult.RuleBasedContentFilter(properties);
	}

	@Test
	void approvesRegularContent() {
		var result = filter.evaluate("Uma explicação sobre Spring Boot.");

		assertThat(result.decision()).isEqualTo(ContentFilterDecision.APPROVED);
		assertThat(result.matchedTerms()).isEmpty();
		assertThat(result.ruleVersion()).isEqualTo("test-v1");
	}

	@Test
	void holdsExactBadWordForHumanReview() {
		var result = filter.evaluate("Que resposta idiota!");

		assertThat(result.requiresReview()).isTrue();
		assertThat(result.matchedTerms()).containsExactly("idiota");
	}

	@Test
	void detectsAccentsLeetspeakSeparatorsAndRepeatedLetters() {
		assertThat(filter.evaluate("P.O.R.R.Á").requiresReview()).isTrue();
		assertThat(filter.evaluate("1d10t4").requiresReview()).isTrue();
		assertThat(filter.evaluate("poooorrra").requiresReview()).isTrue();
	}

	@Test
	void doesNotMatchSubstringOrConfiguredException() {
		assertThat(filter.evaluate("Isso virou uma porradaria.").requiresReview()).isFalse();
		assertThat(filter.evaluate("Texto digitado sem ofensa.").requiresReview()).isFalse();
	}

	@Test
	void handlesNullAndBlankContent() {
		assertThat(filter.evaluate(null).requiresReview()).isFalse();
		assertThat(filter.evaluate("   ").requiresReview()).isFalse();
	}
}
