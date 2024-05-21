package io.autoflow.app.mapper;

import io.autoflow.app.model.Workflow;
import io.autoflow.app.vo.WorkflowVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author yiuman
 * @date 2024/5/21
 */
@Mapper
public interface FlowMapper {
    FlowMapper INSTANCE = Mappers.getMapper(FlowMapper.class);

    WorkflowVO toWorkflowVO(Workflow workflow);
}