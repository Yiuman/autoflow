package io.autoflow.plugin.regex;

import lombok.Data;

@Data
public class RegexParameter {
    private String regex;
    private String content;
    private RegexMethod method;
    private String replace;
}
