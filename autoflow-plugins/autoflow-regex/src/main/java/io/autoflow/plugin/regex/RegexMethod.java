package io.autoflow.plugin.regex;

import cn.hutool.core.util.ReUtil;
import lombok.Getter;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Getter
public enum RegexMethod {
    split(p -> p.getContent().split(p.getRegex())),
    findAll(p -> ReUtil.findAll(p.getRegex(), p.getContent(), p.getGroup())),
    findFirst(p -> {
        Pattern pattern = Pattern.compile(p.getRegex());
        Matcher matcher = pattern.matcher(p.getContent());
        if (matcher.find()) {
            return matcher.group(p.getGroup());
        }
        return null;
    }),
    isMatch(p -> ReUtil.isMatch(p.getRegex(), p.getContent())),
    replace(p -> p.getContent().replace(p.getRegex(), p.getReplace())),
    replaceAll(p -> p.getContent().replaceAll(p.getRegex(), p.getReplace()));

    private final Function<RegexParameter, Object> func;

    RegexMethod(Function<RegexParameter, Object> func) {
        this.func = func;
    }

}
