package io.autoflow.app.service;

import io.autoflow.app.model.ChatSession;
import io.ola.crud.service.CrudService;

public interface ChatSessionService extends CrudService<ChatSession> {

    void generateTitle(String sessionId);
}
