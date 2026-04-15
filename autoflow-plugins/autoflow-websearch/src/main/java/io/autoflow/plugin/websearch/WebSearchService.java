package io.autoflow.plugin.websearch;

import io.autoflow.plugin.websearch.constant.SearchResult;
import io.autoflow.plugin.websearch.model.WebSearchParameter;
import io.autoflow.plugin.websearch.model.WebSearchResult;
import io.autoflow.spi.context.ExecutionContext;
import io.autoflow.spi.impl.BaseService;

import java.util.List;

/**
 * Web search service
 *
 * @author yiuman
 * @date 2026/4/15
 */
public class WebSearchService extends BaseService<WebSearchParameter, WebSearchResult> {

    private final WebSearchProvider provider;

    public WebSearchService() {
        this.provider = new DuckDuckGoProvider();
    }

    public WebSearchService(WebSearchProvider provider) {
        this.provider = provider;
    }

    @Override
    public String getName() {
        return "WebSearch";
    }

    @Override
    public WebSearchResult execute(WebSearchParameter parameter, ExecutionContext context) {
        List<SearchResult> results = provider.search(
                parameter.getQuery(),
                parameter.getMaxResults() != null ? parameter.getMaxResults() : 5
        );

        WebSearchResult webSearchResult = new WebSearchResult();
        webSearchResult.setResults(results);
        return webSearchResult;
    }
}