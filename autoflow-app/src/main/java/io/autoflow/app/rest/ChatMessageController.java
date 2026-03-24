package io.autoflow.app.rest;

import io.autoflow.app.model.ChatMessage;
import io.autoflow.app.query.ChatMessageQuery;
import io.ola.crud.query.annotation.Query;
import io.ola.crud.rest.BaseRESTAPI;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat/messages")
@Query(ChatMessageQuery.class)
public class ChatMessageController implements BaseRESTAPI<ChatMessage> {
}
