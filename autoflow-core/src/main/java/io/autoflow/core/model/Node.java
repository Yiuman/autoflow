package io.autoflow.core.model;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;
import java.util.Objects;

/**
 * 节点
 *
 * @author yiuman
 * @date 2023/7/13
 */
@Data
@EqualsAndHashCode(of = "id")
public class Node {
    private String id;
    private String parentId;
    private String label;
    /**
     * 节点的静态配置参数
     */
    private Map<String, Object> data;
    /**
     * 节点在前端显示的位置定位
     */
    private Position position;
    /**
     * 节点类型
     */
    private NodeType type;
    /**
     * 实现类（即插件）
     */
    private String serviceId;
    /**
     * 循环参数
     */
    private Loop loop;

    public boolean loopIsValid() {
        return isLoop() || isWhile();
    }

    public boolean isLoop() {
        if (Objects.isNull(loop)) {
            return false;
        }
        return StrUtil.isNotBlank(loop.getCollectionString())
                || (Objects.nonNull(loop.getLoopCardinality()) && loop.getLoopCardinality() > 0);
    }

    public boolean isWhile() {
        if (Objects.isNull(loop)) {
            return false;
        }
        return StrUtil.isBlank(loop.getCollectionString())
                && (Objects.isNull(loop.getLoopCardinality()) || loop.getLoopCardinality() <= 0)
                && StrUtil.isNotBlank(loop.getCompletionCondition());
    }
}
