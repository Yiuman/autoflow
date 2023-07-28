package io.autoflow.app.rest;

import io.autoflow.common.http.R;
import io.autoflow.core.model.Flow;
import io.autoflow.core.model.Node;
import io.autoflow.core.runtime.Executor;
import io.autoflow.spi.model.ExecutionData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    private final Executor executor;

    @PostMapping
    public R<Map<String, List<ExecutionData>>> execute(@RequestBody Flow flow) {
        return R.ok(executor.execute(flow));
    }

    @PostMapping("/node")
    public R<List<ExecutionData>> executeNode(@RequestBody Node node) {
        return R.ok(executor.executeNode(node));
    }
}
