package io.autoflow.plugin.websearch.model;

import io.autoflow.plugin.websearch.constant.SearchResult;
import lombok.Data;

import java.util.List;

/**
 * Web search output result
 *
 * @author yiuman
 * @date 2026/4/15
 */
@Data
public class WebSearchResult {
    private List<SearchResult> results;
}