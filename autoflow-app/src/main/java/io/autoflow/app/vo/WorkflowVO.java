package io.autoflow.app.vo;

import io.autoflow.app.model.Tag;
import io.autoflow.app.model.Workflow;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author yiuman
 * @date 2024/5/21
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class WorkflowVO extends Workflow {
    private List<Tag> tags;
}
