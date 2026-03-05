package io.autoflow.app.service;

import io.autoflow.app.model.ToolCall;
import io.ola.crud.service.CrudService;
import org.springframework.stereotype.Service;

/**
 * 工具调用服务
 *
 * @author autoflow
 * @date 2025/03/04
 */
@Service
public interface ToolCallService extends CrudService<ToolCall> {

}
