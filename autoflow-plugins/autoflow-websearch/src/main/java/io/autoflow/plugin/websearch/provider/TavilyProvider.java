package io.autoflow.plugin.websearch.provider;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import io.autoflow.plugin.websearch.WebSearchProvider;
import io.autoflow.plugin.websearch.constant.SearchResult;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Tavily web search provider
 *
 * @author yiuman
 * @date 2026/4/15
 */
public class TavilyProvider implements WebSearchProvider {

    private static final String TAVILY_API_URL = "https://api.tavily.com/search";

    private final String apiKey;

    public TavilyProvider(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public List<SearchResult> search(String query, int maxResults) {
        String body = JSONUtil.toJsonStr(new JSONObject()
                .set("api_key", apiKey)
                .set("query", query)
                .set("max_results", maxResults));

        try (HttpResponse response = HttpRequest.post(TAVILY_API_URL)
                .body(body)
                .contentType("application/json")
                .execute()) {

            JSONObject json = JSONUtil.parseObj(response.body());
            cn.hutool.json.JSONArray resultsArray = json.getJSONArray("results");

            List<SearchResult> searchResults = new ArrayList<>();
            for (int i = 0; i < resultsArray.size(); i++) {
                JSONObject result = resultsArray.getJSONObject(i);
                searchResults.add(new SearchResult(
                        result.getStr("title"),
                        result.getStr("url"),
                        result.getStr("content")
                ));
            }
            return searchResults;
        }
    }
}
