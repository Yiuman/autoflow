package io.autoflow.plugin.shell;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import io.autoflow.spi.context.ExecutionContext;
import io.autoflow.spi.exception.InputValidateException;
import io.autoflow.spi.impl.BaseService;
import io.autoflow.spi.model.FileData;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;

/**
 * @author yiuman
 * @date 2025/1/22
 */
public class ShellService extends BaseService<ShellParameter, ShellResult> {

    @Override
    public String getName() {
        return "Shell";
    }

    @Override
    public ShellResult execute(ShellParameter shellParameter, ExecutionContext executionContext) {
        String shellRaw = shellParameter.getShellRaw();
        if (StrUtil.isBlank(shellRaw)) {
            FileData shellFile = shellParameter.getShellFile();
            Assert.notNull(shellRaw, () -> new InputValidateException("shellRaw or shellFile cannot be null"));
            shellRaw = StrUtil.str(shellFile.getContent(), StandardCharsets.UTF_8);
        }

        CommandLine commandLine = CommandLine.parse(shellRaw);
        if (CollUtil.isNotEmpty(shellParameter.getArgs())) {
            for (String arg : shellParameter.getArgs()) {
                if (StrUtil.isBlank(arg)) {
                    continue;
                }
                commandLine.addArgument(arg);
            }
        }
        //接收正常结果流
        ByteArrayOutputStream successStream = new ByteArrayOutputStream();
        //接收异常结果流
        ByteArrayOutputStream failStream = new ByteArrayOutputStream();
        DefaultExecutor executor = DefaultExecutor.builder().get();
        PumpStreamHandler streamHandler = new PumpStreamHandler(successStream, failStream);
        executor.setStreamHandler(streamHandler);

        if (Objects.nonNull(shellParameter.getDuration())) {
            ExecuteWatchdog executeWatchdog = new ExecuteWatchdog.Builder()
                    .setTimeout(Duration.ofMillis(shellParameter.getDuration()))
                    .get();
            executor.setWatchdog(executeWatchdog);
        }

        int statusCode = -1;
        String output;
        try {
            statusCode = executor.execute(commandLine);
            ByteArrayOutputStream stream = executor.isFailure(statusCode) ? failStream : successStream;
            output = StrUtil.str(stream.toByteArray(), Charset.defaultCharset());
        } catch (IOException e) {
            output = e.getMessage();
        }
        return ShellResult.builder().exitStatusCode(statusCode).output(output).build();
    }
}
