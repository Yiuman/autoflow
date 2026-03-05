package io.autoflow.app.service.impl;

import io.autoflow.app.model.ChatMessage;
import io.autoflow.app.service.ChatMessageService;
import io.ola.crud.service.impl.BaseService;
import org.springframework.stereotype.Service;

/**
 * 聊天消息服务实现
 *
 * @author autoflow
 * @date 2025/03/04
 */
@Service
public class ChatMessageServiceImpl extends BaseService<ChatMessage> implements ChatMessageService {

}
