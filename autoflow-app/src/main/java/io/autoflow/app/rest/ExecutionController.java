package io.autoflow.app.rest;

import cn.hutool.core.thread.ThreadUtil;
import io.autoflow.app.flowable.SSEContext;
import io.autoflow.app.request.StopRequest;
import io.autoflow.common.http.R;
import io.autoflow.core.model.Flow;
import io.autoflow.core.model.Node;
import io.autoflow.core.runtime.Executor;
import io.autoflow.spi.model.ExecutionData;
import lombok.RequiredArgsConstructor;
import org.flowable.engine.RuntimeService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

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
    private final RuntimeService runtimeService;
    private final Executor executor;

    @PostMapping
    public R<Map<String, List<ExecutionData>>> execute(@RequestBody Flow flow) {
        return R.ok(executor.execute(flow));
    }

    @PostMapping("/getExecutableId")
    public R<String> executableId(@RequestBody Flow flow) {
        return R.ok(executor.getExecutableId(flow));
    }

    @PostMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter executeSSE(@RequestBody Flow flow) {
        SseEmitter sseEmitter = new SseEmitter(0L);
        String executableId = executor.getExecutableId(flow);
        SSEContext.add(executableId, sseEmitter);
        ThreadUtil.execute(() -> runtimeService.startProcessInstanceById(executableId));
        return sseEmitter;
    }

    @PostMapping("/stop")
    public R<Void> stop(@RequestBody StopRequest stopRequest) {
        //todo 根据类型去停止在执行的任务
        return R.ok();
    }

    @PostMapping("/node")
    public R<List<ExecutionData>> executeNode(@RequestBody Node node) {
        return R.ok(executor.executeNode(node));
    }

}
