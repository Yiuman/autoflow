package io.autoflow.plugin.websearch;

import io.autoflow.plugin.websearch.constant.SearchResult;

import java.util.List;

/**
 * Web search provider interface
 * Implement this to add new search backends (Tavily, Google, Bing, etc.)
 *
 * @author yiuman
 * @date 2026/4/15
 */
public interface WebSearchProvider {

    /**
     * Search the web for the given query
     *
     * @param query the search query
     * @param maxResults maximum number of results to return
     * @return list of search results
     */
    List<SearchResult> search(String query, int maxResults);
}
