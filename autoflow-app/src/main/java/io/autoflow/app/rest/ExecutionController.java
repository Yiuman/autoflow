package io.autoflow.app.rest;

import cn.hutool.core.thread.ThreadUtil;
import io.autoflow.common.http.SSEContext;
import io.autoflow.core.model.Flow;
import io.autoflow.core.model.Node;
import io.autoflow.core.runtime.Executor;
import io.autoflow.spi.model.ExecutionResult;
import io.autoflow.spi.model.FlowExecutionResult;
import io.ola.common.http.R;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * 执行接口
 *
 * @author yiuman
 * @date 2023/7/26
 */
@RestController
@RequestMapping("/executions")
@RequiredArgsConstructor
public class ExecutionController {
    private final Executor executor;

    @PostMapping
    public R<FlowExecutionResult> execute(@RequestBody Flow flow) {
        return R.ok(executor.execute(flow));
    }

    @PostMapping("/getExecutableId")
    public R<String> executableId(@RequestBody Flow flow) {
        return R.ok(executor.getExecutableId(flow));
    }

    @PostMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter executeSse(@RequestBody Flow flow) {
        SseEmitter sseEmitter = new SseEmitter(0L);
        String executableId = executor.getExecutableId(flow);
        SSEContext.add(executableId, sseEmitter);
        ThreadUtil.execute(() -> executor.startByExecutableId(executableId));
        return sseEmitter;
    }

    @PostMapping("/node")
    public R<List<ExecutionResult<Object>>> executeNode(@RequestBody Node node) {
        return R.ok(executor.executeNode(node));
    }

}
