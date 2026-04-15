package io.autoflow.plugin.websearch.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Web search input parameters
 *
 * @author yiuman
 * @date 2026/4/15
 */
@Data
public class WebSearchParameter {
    @NotNull
    private String query;
    private Integer maxResults = 5;
}