package io.autoflow.app.rest;

import cn.hutool.core.util.IdUtil;
import io.autoflow.app.model.ChatSession;
import io.autoflow.app.model.CreateSessionRequest;
import io.autoflow.app.query.ChatSessionQuery;
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

    public ChatSessionController(ChatSessionService chatSessionService) {
        this.chatSessionService = chatSessionService;
    }

    @PostMapping("/create")
    public R<String> createSession(@RequestBody CreateSessionRequest request) {
        String sessionId = IdUtil.fastSimpleUUID();
        ChatSession session = new ChatSession();
        session.setId(sessionId);
        session.setModelId(request != null ? request.getModelId() : null);
        session.setStatus("ACTIVE");
        chatSessionService.save(session);
        log.info("Created new session: sessionId={}, modelId={}", sessionId, request != null ? request.getModelId() : null);
        return R.ok(sessionId);
    }
}
