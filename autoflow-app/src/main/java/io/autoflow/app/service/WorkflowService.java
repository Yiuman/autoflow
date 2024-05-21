package io.autoflow.app.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import io.autoflow.app.mapper.FlowMapper;
import io.autoflow.app.model.Tag;
import io.autoflow.app.model.Workflow;
import io.autoflow.app.vo.WorkflowVO;
import io.autoflow.core.model.Flow;
import io.autoflow.core.model.Node;
import io.ola.crud.service.impl.BaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yiuman
 * @date 2024/5/20
 */
@Service
@RequiredArgsConstructor
public class WorkflowService extends BaseService<Workflow> {
    private final TagService tagService;

    @Override
    public <T extends Workflow> void beforeSave(T entity) {
        super.beforeSave(entity);
        if (StrUtil.isNotBlank(entity.getFlowStr())) {
            Flow flow = JSONUtil.toBean(entity.getFlowStr(), Flow.class);
            List<Node> nodes = flow.getNodes();
            entity.setPluginIds(nodes.stream().map(Node::getServiceId).distinct().toList());
        }
    }


    @SuppressWarnings("unchecked")
    @Override
    public <T extends Workflow> Page<T> page(Page<Workflow> page, QueryWrapper queryWrapper) {
        Page<T> workflowPage = super.page(page, queryWrapper);
        List<T> records = workflowPage.getRecords();
        List<String> allTagIds = records.stream()
                .filter(item -> CollUtil.isNotEmpty(item.getTagIds()))
                .flatMap(item -> item.getTagIds().stream())
                .collect(Collectors.toList());
        List<Tag> list = tagService.list(allTagIds);
        List<WorkflowVO> vos = records.stream().map(workflow -> {
            WorkflowVO workflowVO = FlowMapper.INSTANCE.toWorkflowVO(workflow);
            workflowVO.setTags(list.stream()
                    .filter(tag -> CollUtil.contains(workflow.getTagIds(), tag.getId()))
                    .collect(Collectors.toList()));
            return workflowVO;
        }).toList();
        workflowPage.setRecords((List<T>) vos);
        return workflowPage;
    }
}
