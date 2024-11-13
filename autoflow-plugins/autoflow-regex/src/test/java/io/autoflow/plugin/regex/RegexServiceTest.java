package io.autoflow.plugin.regex;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class RegexServiceTest {

    @Test
    public void testSpilt() {
        RegexService regexService = new RegexService();
        RegexParameter regexParameter = new RegexParameter();
        regexParameter.setContent("谎言是一只心灵的蛀虫，将人的心蛀得面目全非；" +
                "谎言是一个深深的泥潭，让人深陷其中无法自拔；" +
                "谎言是一个无尽的黑洞，让人坠入罪恶的深渊万劫不复");
        regexParameter.setRegex("；");
        regexParameter.setMethod(RegexMethod.split);
        RegexResult regexResult = regexService.execute(regexParameter, null);
        log.info(JSONUtil.toJsonStr(regexResult));
    }

    @Test
    public void testFindAll() {
        RegexService regexService = new RegexService();
        RegexParameter regexParameter = new RegexParameter();
        regexParameter.setContent("谎言是一只心灵的蛀虫，将人的心蛀得面目全非；" +
                "谎言是一个深深的泥潭，让人深陷其中无法自拔；" +
                "谎言是一个无尽的黑洞，让人坠入罪恶的深渊万劫不复");
        regexParameter.setRegex("谎言");
        regexParameter.setMethod(RegexMethod.findAll);
        RegexResult regexResult = regexService.execute(regexParameter, null);
        log.info(JSONUtil.toJsonStr(regexResult));
    }

    @Test
    public void testIsMatch() {
        RegexService regexService = new RegexService();
        RegexParameter regexParameter = new RegexParameter();
        regexParameter.setContent("爱上你，只用了一瞬间。");
        regexParameter.setRegex("^爱上你.*");
        regexParameter.setMethod(RegexMethod.isMatch);
        RegexResult regexResult = regexService.execute(regexParameter, null);
        log.info(JSONUtil.toJsonStr(regexResult));
        regexParameter.setRegex("什么鬼");
        regexResult = regexService.execute(regexParameter, null);
        log.info(JSONUtil.toJsonStr(regexResult));
    }

    @Test
    public void testReplace() {
        RegexService regexService = new RegexService();
        RegexParameter regexParameter = new RegexParameter();
        regexParameter.setContent("爱上你，只用了一瞬间。");
        regexParameter.setRegex("爱");
        regexParameter.setReplace("");
        regexParameter.setMethod(RegexMethod.replace);
        RegexResult regexResult = regexService.execute(regexParameter, null);
        log.info(JSONUtil.toJsonStr(regexResult));
    }

    @Test
    public void testReplaceAll() {
        RegexService regexService = new RegexService();
        RegexParameter regexParameter = new RegexParameter();
        regexParameter.setContent("谎言是一只心灵的蛀虫，将人的心蛀得面目全非；" +
                "谎言是一个深深的泥潭，让人深陷其中无法自拔；" +
                "谎言是一个无尽的黑洞，让人坠入罪恶的深渊万劫不复");
        regexParameter.setRegex("谎言");
        regexParameter.setReplace("旺旺");
        regexParameter.setMethod(RegexMethod.replaceAll);
        RegexResult regexResult = regexService.execute(regexParameter, null);
        log.info(JSONUtil.toJsonStr(regexResult));
    }

}