package io.autoflow.plugin.websearch.provider;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import io.autoflow.plugin.websearch.WebSearchProvider;
import io.autoflow.plugin.websearch.constant.SearchResult;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * DuckDuckGo web search provider using direct HTTP calls
 *
 * @author yiuman
 * @date 2026/4/15
 */
public class DuckDuckGoProvider implements WebSearchProvider {

    private static final String SEARCH_URL = "https://duckduckgo.com/html/";

    @Override
    public List<SearchResult> search(String query, int maxResults) {
        String url = SEARCH_URL + "?q=" + encode(query);

        try (HttpResponse response = HttpRequest.get(url)
                .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36")
                .execute()) {

            String html = response.body();
            return parseResults(html, maxResults);
        } catch (Exception e) {
            throw new RuntimeException("DuckDuckGo search failed", e);
        }
    }

    private String encode(String query) {
        return java.net.URLEncoder.encode(query, java.nio.charset.StandardCharsets.UTF_8);
    }

    private List<SearchResult> parseResults(String html, int maxResults) {
        List<SearchResult> results = new ArrayList<>();

        Document doc = Jsoup.parse(html);
        Elements resultElements = doc.select(".result");

        int count = 0;
        for (Element element : resultElements) {
            if (count >= maxResults) break;

            Element linkElement = element.selectFirst(".result__a");
            Element snippetElement = element.selectFirst(".result__snippet");

            if (linkElement != null) {
                String title = linkElement.text();
                String url = linkElement.attr("href");
                String snippet = snippetElement != null ? snippetElement.text() : "";

                results.add(new SearchResult(title, url, snippet));
                count++;
            }
        }

        return results;
    }
}