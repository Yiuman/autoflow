package io.autoflow.plugin.websearch.provider;

import dev.langchain4j.web.search.WebSearchEngine;
import dev.langchain4j.web.search.WebSearchResults;
import io.autoflow.plugin.websearch.WebSearchProvider;
import io.autoflow.plugin.websearch.constant.SearchResult;

import java.util.List;
import java.util.stream.Collectors;

/**
 * DuckDuckGo web search provider using LangChain4j community module
 *
 * @author yiuman
 * @date 2026/4/15
 */
public class DuckDuckGoProvider implements WebSearchProvider {

    private final WebSearchEngine searchEngine;

    public DuckDuckGoProvider() {
        this.searchEngine = dev.langchain4j.community.web.search.duckduckgo.DuckDuckGoWebSearchEngine.builder().build();
    }

    @Override
    public List<SearchResult> search(String query, int maxResults) {
        WebSearchResults response = searchEngine.search(query);

        return response.results().stream()
                .limit(maxResults)
                .map(result -> new SearchResult(
                        result.title(),
                        result.url().toString(),
                        result.snippet() != null ? result.snippet() : ""
                ))
                .collect(Collectors.toList());
    }
}
