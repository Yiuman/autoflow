package io.autoflow.plugin.websearch;

import io.autoflow.plugin.websearch.constant.SearchResult;
import io.autoflow.plugin.websearch.model.WebSearchParameter;
import io.autoflow.plugin.websearch.model.WebSearchResult;
import io.autoflow.plugin.websearch.provider.DuckDuckGoProvider;
import io.autoflow.plugin.websearch.provider.TavilyProvider;
import io.autoflow.spi.context.ExecutionContext;
import io.autoflow.spi.impl.BaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Web search service
 *
 * @author yiuman
 * @date 2026/4/15
 */
public class WebSearchService extends BaseService<WebSearchParameter, WebSearchResult> {

    private static final Logger LOG = LoggerFactory.getLogger(WebSearchService.class);

    private static final String PROVIDER_TYPE_KEY = "autoflow.websearch.provider";
    private static final String TAVILY_API_KEY_KEY = "autoflow.websearch.tavily.api-key";

    private static final String PROVIDER_DUCKDUCKGO = "duckduckgo";
    private static final String PROVIDER_TAVILY = "tavily";

    private final WebSearchProvider provider;

    public WebSearchService() {
        this.provider = createProvider();
    }

    public WebSearchService(WebSearchProvider provider) {
        this.provider = provider;
    }

    private WebSearchProvider createProvider() {
        String providerType = System.getProperty(PROVIDER_TYPE_KEY, PROVIDER_DUCKDUCKGO);

        return switch (providerType.toLowerCase()) {
            case PROVIDER_TAVILY -> {
                String apiKey = System.getProperty(TAVILY_API_KEY_KEY);
                if (apiKey == null || apiKey.isBlank()) {
                    LOG.warn("Tavily API key not configured, falling back to DuckDuckGo");
                    yield new DuckDuckGoProvider();
                }
                LOG.info("Using Tavily web search provider");
                yield new TavilyProvider(apiKey);
            }
            default -> {
                LOG.info("Using DuckDuckGo web search provider");
                yield new DuckDuckGoProvider();
            }
        };
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