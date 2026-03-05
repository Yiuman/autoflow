package io.autoflow.app.service;

import io.autoflow.app.model.ChatSession;
import io.ola.crud.service.CrudService;
import org.springframework.stereotype.Service;

/**
 * 聊天会话服务
 *
 * @author autoflow
 * @date 2025/03/04
 */
@Service
public interface ChatSessionService extends CrudService<ChatSession> {

}
