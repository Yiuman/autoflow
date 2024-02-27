package io.autoflow.plugin.regex;

import cn.hutool.json.JSONUtil;
import io.autoflow.spi.model.ExecutionData;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;

@Slf4j
class RegexServiceTest {

    @Test
    public void testRegexService(){
        RegexService regexService = new RegexService();
        RegexParameter regexParameter = new RegexParameter();
        regexParameter.setContent("爱上你，只用了一瞬间。");
        regexParameter.setRegex("爱");
        regexParameter.setReplace("");
        regexParameter.setMethod(RegexMethod.replace);
        List<ExecutionData> execute = regexService.execute(regexParameter);
        log.info(JSONUtil.toJsonStr(execute));
    }
}