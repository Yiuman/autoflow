package io.autoflow.plugin.websearch;

import cn.hutool.json.JSONUtil;
import io.autoflow.plugin.websearch.constant.SearchResult;
import io.autoflow.plugin.websearch.model.WebSearchParameter;
import io.autoflow.plugin.websearch.model.WebSearchResult;
import io.autoflow.plugin.websearch.provider.DuckDuckGoProvider;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * WebSearchService tests
 *
 * @author yiuman
 * @date 2026/4/15
 */
@Slf4j
class WebSearchServiceTest {

    @Test
    public void testDuckDuckGoSearch() {
        WebSearchService service = new WebSearchService();
        WebSearchParameter parameter = new WebSearchParameter();
        parameter.setQuery("中华人民共和国民法典第十二条");
        parameter.setMaxResults(3);

        WebSearchResult result = service.execute(parameter, null);

        assertNotNull(result);
        assertNotNull(result.getResults());
        assertFalse(result.getResults().isEmpty());
        assertTrue(result.getResults().size() <= 3);

        for (SearchResult item : result.getResults()) {
            assertNotNull(item.title());
            assertNotNull(item.url());
            log.info("Title: {}, URL: {}, Snippet: {}", item.title(), item.url(), item.snippet());
        }

        log.info("Search results: {}", JSONUtil.toJsonStr(result));
    }

    @Test
    public void testDefaultMaxResults() {
        WebSearchService service = new WebSearchService();
        WebSearchParameter parameter = new WebSearchParameter();
        parameter.setQuery("Java programming");

        WebSearchResult result = service.execute(parameter, null);

        assertNotNull(result);
        assertNotNull(result.getResults());
    }

    @Test
    public void testServiceName() {
        WebSearchService service = new WebSearchService();
        assertEquals("WebSearch", service.getName());
    }

    @Test
    public void testCustomProvider() {
        // Test with a custom mock-like provider that returns predefined results
        WebSearchProvider customProvider = (query, maxResults) -> List.of(
                new SearchResult("Custom Title 1", "https://example.com/1", "Custom snippet 1"),
                new SearchResult("Custom Title 2", "https://example.com/2", "Custom snippet 2")
        );

        WebSearchService service = new WebSearchService(customProvider);
        WebSearchParameter parameter = new WebSearchParameter();
        parameter.setQuery("test");
        parameter.setMaxResults(5);

        WebSearchResult result = service.execute(parameter, null);

        assertNotNull(result);
        assertEquals(2, result.getResults().size());
        assertEquals("Custom Title 1", result.getResults().get(0).title());
        assertEquals("https://example.com/1", result.getResults().get(0).url());

        log.info("Custom provider results: {}", JSONUtil.toJsonStr(result));
    }

    @Test
    public void testEmptyQuery() {
        // Test with a custom provider that returns empty for blank query
        WebSearchProvider customProvider = (query, maxResults) -> {
            if (query == null || query.isBlank()) {
                return List.of();
            }
            return List.of(new SearchResult("Result", "https://example.com", "Snippet"));
        };

        WebSearchService service = new WebSearchService(customProvider);
        WebSearchParameter parameter = new WebSearchParameter();
        parameter.setQuery("");
        parameter.setMaxResults(5);

        WebSearchResult result = service.execute(parameter, null);

        assertNotNull(result);
        assertNotNull(result.getResults());
        assertTrue(result.getResults().isEmpty());
    }
}
