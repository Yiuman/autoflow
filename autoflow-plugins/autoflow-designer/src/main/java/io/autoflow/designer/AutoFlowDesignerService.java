package io.autoflow.designer;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import io.autoflow.spi.context.ExecutionContext;
import io.autoflow.spi.impl.BaseService;

import java.util.Map;

/**
 * AutoFlow Designer Service
 * <p>
 * Provides a tool for LLM to design and generate workflows from natural language.
 * The LLM should call this tool at the end of its analysis to output the workflow structure.
 *
 * @author yiuman
 * @date 2024/XX/XX
 */
public class AutoFlowDesignerService extends BaseService<DesignerParameter, Flow> {

    @Override
    public String getName() {
        return "AutoFlowDesigner";
    }

    @Override
    public Flow execute(DesignerParameter parameter, ExecutionContext ctx) {
        String workflowJson = parameter.getWorkflowJson();
        return JSONUtil.toBean(workflowJson, Flow.class);
    }

    @Override
    protected DesignerParameter buildInput(ExecutionContext executionContext) {
        Map<String, Object> parameters = executionContext.getParameters();
        return BeanUtil.toBean(parameters, DesignerParameter.class);
    }
}
