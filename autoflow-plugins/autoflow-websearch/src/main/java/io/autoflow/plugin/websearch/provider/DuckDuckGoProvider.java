package io.autoflow.plugin.websearch.provider;

import dev.langchain4j.webdawn.client.DuckDuckGoWebSearchClient;
import dev.langchain4j.webdawn.client.WebSearchRequest;
import dev.langchain4j.webdawn.client.WebSearchResponse;
import io.autoflow.plugin.websearch.WebSearchProvider;
import io.autoflow.plugin.websearch.constant.SearchResult;

import java.util.List;
import java.util.stream.Collectors;

/**
 * DuckDuckGo web search provider using LangChain4j
 *
 * @author yiuman
 * @date 2026/4/15
 */
public class DuckDuckGoProvider implements WebSearchProvider {

    private final DuckDuckGoWebSearchClient client;

    public DuckDuckGoProvider() {
        this.client = DuckDuckGoWebSearchClient.create();
    }

    @Override
    public List<SearchResult> search(String query, int maxResults) {
        WebSearchRequest request = WebSearchRequest.builder()
                .query(query)
                .maxResults(maxResults)
                .build();

        WebSearchResponse response = client.execute(request);

        return response.results().stream()
                .map(result -> new SearchResult(
                        result.title(),
                        result.url(),
                        result.snippet()
                ))
                .collect(Collectors.toList());
    }
}