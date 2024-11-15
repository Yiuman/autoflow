package io.autoflow.app.rest;

import io.autoflow.app.model.Workflow;
import io.autoflow.app.model.WorkflowInst;
import io.autoflow.app.service.ExecutionService;
import io.autoflow.common.http.SSEContext;
import io.autoflow.core.model.Node;
import io.autoflow.core.runtime.Executor;
import io.autoflow.spi.model.ExecutionResult;
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
    private final ExecutionService executionService;

    @PostMapping
    public R<WorkflowInst> execute(@RequestBody Workflow workflow) {
        return R.ok(executionService.execute(workflow));
    }

    @PostMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter executeSse(@RequestBody Workflow workflow) {
        SseEmitter sseEmitter = new SseEmitter(0L);
        WorkflowInst workflowInst = executionService.executeAsync(workflow);
        SSEContext.add(workflowInst.getId(), sseEmitter);
        return sseEmitter;
    }

    @PostMapping("/node")
    public R<List<ExecutionResult<Object>>> executeNode(@RequestBody Node node) {
        Executor executor = executionService.getExecutor();
        return R.ok(executor.executeNode(node));
    }

}
