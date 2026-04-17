package io.autoflow.designer;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Simplified Flow structure for designer output.
 *
 * @author yiuman
 * @date 2024/XX/XX
 */
@Data
public class Flow {
    private String id;
    private String name;
    private String description;
    private List<Node> nodes;
    private List<Connection> connections;
    private Map<String, Object> data;

    @Data
    public static class Node {
        private String id;
        private String label;
        private String type;
        private String serviceId;
        private Map<String, Object> data;
        private Position position;
    }

    @Data
    public static class Connection {
        private String id;
        private String source;
        private String target;
        private String sourcePointType;
        private String targetPointType;
        private String expression;
    }

    @Data
    public static class Position {
        private int x;
        private int y;
    }
}
