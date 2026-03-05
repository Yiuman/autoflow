package io.autoflow.app.service;

import io.autoflow.app.model.ChatMessage;
import io.ola.crud.service.CrudService;
import org.springframework.stereotype.Service;

/**
 * 聊天消息服务
 *
 * @author autoflow
 * @date 2025/03/04
 */
@Service
public interface ChatMessageService extends CrudService<ChatMessage> {

}
