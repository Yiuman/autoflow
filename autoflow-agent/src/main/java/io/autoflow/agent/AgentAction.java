package io.autoflow.agent.spi;

import lombok.Data;

import java.util.Map;

/**
 * @author yiuman
 * @date 2024/10/11
 */
@Data
public class AgentAction {
    private String action;
    private String tool;
    private Map<String, Object> args;
}
