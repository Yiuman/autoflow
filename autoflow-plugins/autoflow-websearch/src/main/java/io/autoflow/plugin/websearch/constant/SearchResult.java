package io.autoflow.plugin.websearch.constant;

/**
 * Single web search result
 *
 * @param title result title
 * @param url result URL
 * @param snippet result snippet
 * @author yiuman
 * @date 2026/4/15
 */
public record SearchResult(String title, String url, String snippet) {
}