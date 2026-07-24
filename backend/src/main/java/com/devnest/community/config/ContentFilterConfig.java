package com.devnest.community.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
		ContentFilterProperties.class,
		CommunityLimitsProperties.class
})
public class ContentFilterConfig {
}
