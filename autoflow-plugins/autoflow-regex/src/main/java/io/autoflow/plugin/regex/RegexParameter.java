package io.autoflow.plugin.regex;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegexParameter {
    @NotNull
    private String regex;
    @NotNull
    private String content;
    @NotNull
    private RegexMethod method;
    /**
     * 分组
     */
    private Integer group = 0;
    private String replace;
}
