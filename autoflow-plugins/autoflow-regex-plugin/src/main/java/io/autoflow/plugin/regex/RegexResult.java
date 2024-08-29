package io.autoflow.plugin.regex;

import lombok.Data;

import java.util.List;

/**
 * @author yiuman
 * @date 2024/8/29
 */
@Data
public class RegexResult {
    private List<String> split;
    private List<String> findAll;
    private String findFirst;
    private Boolean isMatch;
    private String replace;
    private String replaceAll;
}
