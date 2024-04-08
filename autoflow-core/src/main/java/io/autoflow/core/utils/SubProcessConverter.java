package io.autoflow.core.utils;

import cn.hutool.core.util.StrUtil;
import io.autoflow.core.model.Node;
import org.flowable.bpmn.model.SubProcess;

/**
 * @author yiuman
 * @date 2024/4/8
 */
public enum SubProcessConverter implements NodeConverter<SubProcess> {
    INSTANCE;

    @Override
    public SubProcess convert(Node node) {
        SubProcess subProcess = new SubProcess();
        subProcess.setId(node.getId());
        subProcess.setName(node.getLabel());
        subProcess.addFlowElement(Flows.createStartEvent(StrUtil.format("{}_{}", node.getId(), Flows.START_EVENT_ID)));
        subProcess.addFlowElement(Flows.createEndEvent(StrUtil.format("{}_{}", node.getId(), Flows.START_EVENT_ID)));
        Flows.addMultiInstanceLoopCharacteristics(subProcess, node.getLoop());
        return subProcess;
    }
}
