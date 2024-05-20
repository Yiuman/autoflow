package io.autoflow.app.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import io.autoflow.app.model.Workflow;
import io.autoflow.core.model.Flow;
import io.autoflow.core.model.Node;
import io.ola.crud.service.impl.BaseService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author yiuman
 * @date 2024/5/20
 */
@Service
public class WorkflowService extends BaseService<Workflow> {

    @Override
    public <T extends Workflow> void beforeSave(T entity) {
        super.beforeSave(entity);
        if (StrUtil.isNotBlank(entity.getFlowStr())) {
            Flow flow = JSONUtil.toBean(entity.getFlowStr(), Flow.class);
            List<Node> nodes = flow.getNodes();
            entity.setPlugins(nodes.stream().map(Node::getServiceId).distinct().toList());
        }
    }

    @Override
    public Page<Workflow> page(Page<Workflow> page, QueryWrapper queryWrapper) {
        return super.page(page, queryWrapper);
    }
}
