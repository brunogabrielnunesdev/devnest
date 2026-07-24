package com.devnest.community.service.content;

import com.devnest.community.config.ContentFilterProperties;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public record ContentFilterResult(
		ContentFilterDecision decision,
		String ruleVersion,
		Set<String> matchedTerms
) {
	public ContentFilterResult {
		matchedTerms = Set.copyOf(matchedTerms);
	}

	public boolean requiresReview() {
		return decision == ContentFilterDecision.HELD_FOR_REVIEW;
	}

	@Service
	public static class RuleBasedContentFilter implements ContentFilter {

		private static final String SEPARATORS = "[^\\p{L}\\p{N}]*";
		private final ContentFilterProperties properties;

		public RuleBasedContentFilter(ContentFilterProperties properties) {
			this.properties = properties;
		}

		@Override
		public ContentFilterResult evaluate(String content) {
			String normalized = normalize(content);
			String contentWithoutExceptions = maskExceptions(normalized);
			Set<String> matches = new LinkedHashSet<>();

			for (String configuredTerm : properties.getBadWords()) {
				String term = normalize(configuredTerm).replaceAll("[^\\p{L}\\p{N}]", "");
				if (!term.isBlank() && obfuscatedPattern(term).matcher(contentWithoutExceptions).find()) {
					matches.add(configuredTerm);
				}
			}

			ContentFilterDecision decision = matches.isEmpty()
					? ContentFilterDecision.APPROVED
					: ContentFilterDecision.HELD_FOR_REVIEW;
			return new ContentFilterResult(decision, properties.getRuleVersion(), matches);
		}

		String normalize(String content) {
			if (content == null || content.isBlank()) {
				return "";
			}
			String normalized = Normalizer.normalize(content, Normalizer.Form.NFKC)
					.toLowerCase(Locale.ROOT);
			StringBuilder substituted = new StringBuilder();
			Map<String, String> substitutions = properties.getSubstitutions();
			normalized.codePoints().forEach(codePoint -> {
				String character = new String(Character.toChars(codePoint));
				substituted.append(substitutions.getOrDefault(character, character));
			});
			return Normalizer.normalize(substituted, Normalizer.Form.NFD)
					.replaceAll("\\p{M}+", "");
		}

		private String maskExceptions(String normalizedContent) {
			String masked = normalizedContent;
			for (String configuredException : properties.getExceptions()) {
				String exception = normalize(configuredException).replaceAll("[^\\p{L}\\p{N}]", "");
				if (!exception.isBlank()) {
					masked = obfuscatedPattern(exception).matcher(masked).replaceAll(match -> " ".repeat(match.group().length()));
				}
			}
			return masked;
		}

		private Pattern obfuscatedPattern(String normalizedTerm) {
			StringBuilder regex = new StringBuilder("(?<![\\p{L}\\p{N}])");
			normalizedTerm.codePoints().forEach(codePoint -> regex
					.append(Pattern.quote(new String(Character.toChars(codePoint))))
					.append('+')
					.append(SEPARATORS));
			regex.append("(?![\\p{L}\\p{N}])");
			return Pattern.compile(regex.toString(), Pattern.UNICODE_CHARACTER_CLASS);
		}
	}
}
