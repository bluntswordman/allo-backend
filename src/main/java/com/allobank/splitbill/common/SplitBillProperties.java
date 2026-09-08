package com.allobank.splitbill.common;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "split-bill")
public record SplitBillProperties(String githubUsername) {

	public SplitBillProperties {
		if (githubUsername == null || githubUsername.isBlank()) {
			throw new IllegalArgumentException("split-bill.github-username must not be blank");
		}
	}
}
