package io.autoflow.app.service.impl;

import cn.hutool.json.JSONUtil;
import io.autoflow.app.model.ChatMessage;
import io.autoflow.app.model.ToolCall;
import io.autoflow.app.service.ChatMessageService;
import io.autoflow.app.service.ToolCallService;
import io.ola.crud.service.impl.BaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 工具调用服务实现
 *
 * @author autoflow
 * @date 2025/03/04
 */
@Service
@RequiredArgsConstructor
public class ToolCallServiceImpl extends BaseService<ToolCall> implements ToolCallService {

    private static final String TYPE_TOOL_CALL = "tool_call";
    private static final String ROLE_ASSISTANT = "assistant";

    private final ChatMessageService chatMessageService;

    @Override
    public ToolCall save(final ToolCall toolCall) {
        // Save to ToolCall table first
        ToolCall savedToolCall = super.save(toolCall);

        // Also save to ChatMessage table
        saveToolCallAsChatMessage(toolCall);

        return savedToolCall;
    }

    private void saveToolCallAsChatMessage(final ToolCall toolCall) {
        // Get sessionId from parent message if messageId is set
        String sessionId = null;
        if (Objects.nonNull(toolCall.getMessageId())) {
            ChatMessage parentMessage = chatMessageService.get(toolCall.getMessageId());
            if (Objects.nonNull(parentMessage)) {
                sessionId = parentMessage.getSessionId();
            }
        }

        // Build metadata JSON
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("toolName", toolCall.getToolName());
        metadata.put("parameters", toolCall.getParameters());
        metadata.put("result", toolCall.getResult());
        metadata.put("status", toolCall.getStatus());

        // Create and save ChatMessage
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSessionId(sessionId);
        chatMessage.setRole(ROLE_ASSISTANT);
        chatMessage.setType(TYPE_TOOL_CALL);
        chatMessage.setContent(toolCall.getToolName());
        chatMessage.setMetadata(JSONUtil.toJsonStr(metadata));

        chatMessageService.save(chatMessage);
    }

}
