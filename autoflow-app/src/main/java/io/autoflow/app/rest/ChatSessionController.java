package io.autoflow.app.rest;

import cn.hutool.core.util.IdUtil;
import com.mybatisflex.core.query.QueryWrapper;
import io.autoflow.app.model.AgentConfig;
import io.autoflow.app.model.ChatSession;
import io.autoflow.app.model.CreateSessionRequest;
import io.autoflow.app.query.ChatSessionQuery;
import io.autoflow.app.service.AgentConfigService;
import io.autoflow.app.service.ChatSessionService;
import io.ola.common.http.R;
import io.ola.crud.query.annotation.Query;
import io.ola.crud.rest.BaseRESTAPI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/chat/sessions")
@Query(ChatSessionQuery.class)
public class ChatSessionController implements BaseRESTAPI<ChatSession> {

    private final ChatSessionService chatSessionService;
    private final AgentConfigService agentConfigService;

    public ChatSessionController(ChatSessionService chatSessionService,
                                  AgentConfigService agentConfigService) {
        this.chatSessionService = chatSessionService;
        this.agentConfigService = agentConfigService;
    }

    @PostMapping("/create")
    public R<String> createSession(@RequestBody CreateSessionRequest request) {
        String sessionId = IdUtil.fastSimpleUUID();
        ChatSession session = new ChatSession();
        session.setId(sessionId);
        session.setModelId(request != null ? request.getModelId() : null);
        session.setStatus("ACTIVE");

        if (request != null && request.getAgentConfigId() != null) {
            AgentConfig config = agentConfigService.list(QueryWrapper.create()
                    .eq(AgentConfig::getId, request.getAgentConfigId())).stream().findFirst().orElse(null);
            if (config != null && config.getSystemPrompt() != null) {
                session.setSystemPrompt(config.getSystemPrompt());
                log.info("Applied agent config: agentConfigId={}, configName={}",
                        request.getAgentConfigId(), config.getName());
            }
        }

        chatSessionService.save(session);
        log.info("Created new session: sessionId={}, modelId={}, agentConfigId={}",
                sessionId, request != null ? request.getModelId() : null,
                request != null ? request.getAgentConfigId() : null);
        return R.ok(sessionId);
    }
}
