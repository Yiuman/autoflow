package io.autoflow.core.model;

import io.autoflow.core.enums.PointType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 连接（线）
 *
 * @author yiuman
 * @date 2023/7/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Connection {
    /**
     * 源节点ID
     */
    private String source;
    /**
     * 目标节点ID
     */
    private String target;
    /**
     * 表达式
     */
    private String expression;
    /**
     * 连线起始点的类型
     */
    private PointType sourcePointType;
    /**
     * 连线结束点的类型
     */
    private PointType targetPointType;
    /**
     * 源节点连接位置
     */
    private Float sourceX;
    private Float sourceY;
    /**
     * 目标节点连接位置
     */
    private Float targetX;
    private Float targetY;

    public Connection(String source, String target) {
        this.source = source;
        this.target = target;
    }
}
