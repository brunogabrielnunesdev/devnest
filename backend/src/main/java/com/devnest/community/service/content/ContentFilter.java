package com.devnest.community.service.content;

public interface ContentFilter {

	ContentFilterResult evaluate(String content);
}
