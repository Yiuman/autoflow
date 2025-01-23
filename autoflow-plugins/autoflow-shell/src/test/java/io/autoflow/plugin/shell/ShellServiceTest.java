package io.autoflow.plugin.shell;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

/**
 * @author yiuman
 * @date 2025/1/22
 */
@Slf4j
class ShellServiceTest {

    @Test
    public void shellRawTest() {
        ShellService shellService = new ShellService();
        ShellParameter shellParameter = new ShellParameter();
        shellParameter.setShellRaw("echo 你好");
        ShellResult shellResult = shellService.execute(shellParameter);
        log.info(JSONUtil.toJsonStr(shellResult));
    }
}