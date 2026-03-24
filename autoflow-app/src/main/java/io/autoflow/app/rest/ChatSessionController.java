package io.autoflow.app.rest;

import io.autoflow.app.model.ChatSession;
import io.autoflow.app.query.ChatSessionQuery;
import io.ola.crud.query.annotation.Query;
import io.ola.crud.rest.BaseRESTAPI;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat/sessions")
@Query(ChatSessionQuery.class)
public class ChatSessionController implements BaseRESTAPI<ChatSession> {
}
