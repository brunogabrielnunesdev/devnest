package com.devnest.community.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "devnest.community.content-filter")
public class ContentFilterProperties {

	private String ruleVersion = "1";
	private List<String> badWords = new ArrayList<>();
	private List<String> exceptions = new ArrayList<>();
	private Map<String, String> substitutions = defaultSubstitutions();

	public String getRuleVersion() {
		return ruleVersion;
	}

	public void setRuleVersion(String ruleVersion) {
		this.ruleVersion = ruleVersion;
	}

	public List<String> getBadWords() {
		return badWords;
	}

	public void setBadWords(List<String> badWords) {
		this.badWords = badWords == null ? new ArrayList<>() : new ArrayList<>(badWords);
	}

	public List<String> getExceptions() {
		return exceptions;
	}

	public void setExceptions(List<String> exceptions) {
		this.exceptions = exceptions == null ? new ArrayList<>() : new ArrayList<>(exceptions);
	}

	public Map<String, String> getSubstitutions() {
		return substitutions;
	}

	public void setSubstitutions(Map<String, String> substitutions) {
		this.substitutions = substitutions == null
				? defaultSubstitutions()
				: new LinkedHashMap<>(substitutions);
	}

	private static Map<String, String> defaultSubstitutions() {
		Map<String, String> values = new LinkedHashMap<>();
		values.put("0", "o");
		values.put("1", "i");
		values.put("3", "e");
		values.put("4", "a");
		values.put("5", "s");
		values.put("7", "t");
		values.put("@", "a");
		values.put("$", "s");
		return values;
	}
}
