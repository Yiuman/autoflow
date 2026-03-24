# 流式对话功能完整工作计划

## TL;DR

> **目标**: 完善后端流式对话功能，添加数据库存储（Session/Message表），支持模型动态加载和多轮对话历史
>
> **预计工作量**: Large
> **关键路径**: DB Schema → Entity → Service → MemoryStore → ModelRegistry → 集成测试

---

## 一、现有架构

### 已完成
```
✅ ChatController (/chat SSE endpoint)
✅ ChatStreamListener (事件桥接)
✅ ReActAgent (流式处理)
✅ af_model 表 (AI模型配置)
✅ 前端 (chat.ts + ChatInputBar.vue)
```

### 缺失
```
❌ af_chat_session 表
❌ af_chat_message 表
❌ ChatSessionService
❌ ChatMessageService
❌ DatabaseMemoryStore
❌ DynamicModelRegistry
```

---

## 二、数据库设计

### 2.1 Session表 (af_chat_session)
```sql
CREATE TABLE IF NOT EXISTS af_chat_session
(
    id            VARCHAR(32) PRIMARY KEY,
    name          VARCHAR(255),
    model_id      VARCHAR(32),
    system_prompt TEXT,
    status        VARCHAR(32) DEFAULT 'ACTIVE',
    creator       VARCHAR(32),
    create_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 2.2 Message表 (af_chat_message)
```sql
CREATE TABLE IF NOT EXISTS af_chat_message
(
    id              VARCHAR(32) PRIMARY KEY,
    session_id      VARCHAR(32) NOT NULL,
    role            VARCHAR(32) NOT NULL,
    content         TEXT,
    thinking_content TEXT,
    metadata        TEXT,
    create_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES af_chat_session(id) ON DELETE CASCADE
);

CREATE INDEX idx_message_session ON af_chat_message(session_id);
```

---

## 三、实现计划

### Wave 1: Entity + Schema
```
Task 1: 创建 ChatSession Entity
Task 2: 创建 ChatMessage Entity  
Task 3: 更新 schema.sql
```

### Wave 2: Service + Controller
```
Task 4: 创建 ChatSessionService + 实现
Task 5: 创建 ChatSessionController
Task 6: 创建 ChatMessageService + 实现
Task 7: 创建 ChatMessageController
```

### Wave 3: DatabaseMemoryStore
```
Task 8: 创建 DatabaseMemoryStore (实现MemoryStore接口)
Task 9: 修改 BeanConfig 配置
```

### Wave 4: DynamicModelRegistry
```
Task 10: 创建 DynamicModelRegistry
Task 11: 修改 BeanConfig 使用 DynamicModelRegistry
```

### Wave 5: 集成测试
```
Task 12: SSE端点测试
Task 13: 多轮对话测试
```

---

## 四、详细实现

### Task 1: ChatSession Entity

**文件**: `autoflow-app/src/main/java/io/autoflow/app/model/ChatSession.java`

```java
package io.autoflow.app.model;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import io.ola.crud.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Table("af_chat_session")
public class ChatSession extends BaseEntity<String> {
    @Id(keyType = KeyType.Generator, value = KeyGenerators.uuid)
    private String id;
    private String name;
    private String modelId;
    private String systemPrompt;
    private String status; // ACTIVE, ARCHIVED
}
```

---

### Task 2: ChatMessage Entity

**文件**: `autoflow-app/src/main/java/io/autoflow/app/model/ChatMessage.java`

```java
package io.autoflow.app.model;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import io.ola.crud.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Table("af_chat_message")
public class ChatMessage extends BaseEntity<String> {
    @Id(keyType = KeyType.Generator, value = KeyGenerators.uuid)
    private String id;
    private String sessionId;
    private String role; // USER, ASSISTANT, SYSTEM
    private String content;
    private String thinkingContent;
    private String metadata; // JSON格式存储工具调用等信息
}
```

---

### Task 3: 更新 schema.sql

**文件**: `autoflow-app/src/main/resources/sql/schema.sql`

在文件末尾添加:
```sql
-- Chat Session
CREATE TABLE IF NOT EXISTS af_chat_session
(
    id            VARCHAR(32) PRIMARY KEY,
    name          VARCHAR(255),
    model_id      VARCHAR(32),
    system_prompt TEXT,
    status        VARCHAR(32) DEFAULT 'ACTIVE',
    creator       VARCHAR(32),
    create_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Chat Message
CREATE TABLE IF NOT EXISTS af_chat_message
(
    id              VARCHAR(32) PRIMARY KEY,
    session_id      VARCHAR(32) NOT NULL,
    role            VARCHAR(32) NOT NULL,
    content         TEXT,
    thinking_content TEXT,
    metadata        TEXT,
    create_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES af_chat_session(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_message_session ON af_chat_message(session_id);
```

---

### Task 4: ChatSessionService

**文件**: `autoflow-app/src/main/java/io/autoflow/app/service/ChatSessionService.java`
```java
package io.autoflow.app.service;

import io.autoflow.app.model.ChatSession;
import io.ola.crud.service.CrudService;

public interface ChatSessionService extends CrudService<ChatSession> {
}
```

**文件**: `autoflow-app/src/main/java/io/autoflow/app/service/impl/ChatSessionServiceImpl.java`
```java
package io.autoflow.app.service.impl;

import io.autoflow.app.model.ChatSession;
import io.autoflow.app.service.ChatSessionService;
import io.ola.crud.service.impl.BaseService;
import org.springframework.stereotype.Service;

@Service
public class ChatSessionServiceImpl extends BaseService<ChatSession> implements ChatSessionService {
}
```

---

### Task 5: ChatSessionController

**文件**: `autoflow-app/src/main/java/io/autoflow/app/rest/ChatSessionController.java`
```java
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
```

**文件**: `autoflow-app/src/main/java/io/autoflow/app/query/ChatSessionQuery.java`
```java
package io.autoflow.app.query;

import io.ola.crud.query.annotation.Eq;
import lombok.Data;

@Data
public class ChatSessionQuery {
    @Eq
    private String status;
}
```

---

### Task 6: ChatMessageService

**文件**: `autoflow-app/src/main/java/io/autoflow/app/service/ChatMessageService.java`
```java
package io.autoflow.app.service;

import io.autoflow.app.model.ChatMessage;
import io.ola.crud.service.CrudService;

public interface ChatMessageService extends CrudService<ChatMessage> {
}
```

**文件**: `autoflow-app/src/main/java/io/autoflow/app/service/impl/ChatMessageServiceImpl.java`
```java
package io.autoflow.app.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import io.autoflow.app.model.ChatMessage;
import io.autoflow.app.model.table.Tables;
import io.autoflow.app.service.ChatMessageService;
import io.ola.crud.service.impl.BaseService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatMessageServiceImpl extends BaseService<ChatMessage> implements ChatMessageService {
    
    public List<ChatMessage> findBySessionId(String sessionId) {
        return list(QueryWrapper.create()
                .where(Tables.CHAT_MESSAGE.SESSION_ID.eq(sessionId))
                .orderBy(Tables.CHAT_MESSAGE.CREATE_TIME, true));
    }
}
```

---

### Task 7: ChatMessageController

**文件**: `autoflow-app/src/main/java/io/autoflow/app/rest/ChatMessageController.java`
```java
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
```

**文件**: `autoflow-app/src/main/java/io/autoflow/app/query/ChatMessageQuery.java`
```java
package io.autoflow.app.query;

import io.ola.crud.query.annotation.Eq;
import lombok.Data;

@Data
public class ChatMessageQuery {
    @Eq
    private String sessionId;
}
```

---

### Task 8: DatabaseMemoryStore

**文件**: `autoflow-app/src/main/java/io/autoflow/app/repository/DatabaseMemoryStore.java`

```java
package io.autoflow.app.repository;

import com.mybatisflex.core.query.QueryWrapper;
import io.autoflow.agent.AgentContext;
import io.autoflow.agent.MemoryStore;
import io.autoflow.app.model.ChatMessage;
import io.autoflow.app.model.ChatSession;
import io.autoflow.app.service.ChatMessageService;
import io.autoflow.app.service.ChatSessionService;
import io.autoflow.spi.enums.MessageType;
import io.autoflow.spi.model.ChatMessage as SpiChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DatabaseMemoryStore implements MemoryStore {
    
    private final ChatSessionService chatSessionService;
    private final ChatMessageService chatMessageService;
    
    @Override
    public AgentContext load(String sessionId) {
        AgentContext context = new AgentContext(sessionId);
        
        // 加载Session
        ChatSession session = chatSessionService.getById(sessionId);
        if (session != null) {
            context.setSystemPrompt(session.getSystemPrompt());
        }
        
        // 加载Messages
        List<ChatMessage> messages = chatMessageService.findBySessionId(sessionId);
        for (ChatMessage msg : messages) {
            SpiChatMessage spiMsg = new SpiChatMessage();
            spiMsg.setType(MessageType.valueOf(msg.getRole()));
            spiMsg.setContent(msg.getContent());
            context.getMessages().add(spiMsg);
        }
        
        return context;
    }
    
    @Override
    public void save(AgentContext context) {
        // 保存/更新Session
        ChatSession session = chatSessionService.getById(context.getSessionId());
        if (session == null) {
            session = new ChatSession();
            session.setId(context.getSessionId());
        }
        session.setSystemPrompt(context.getSystemPrompt());
        session.setStatus("ACTIVE");
        chatSessionService.save(session);
        
        // 保存Messages (简化处理：删除旧消息，重新保存)
        // 注意：实际生产中应该增量更新
        // 这里为了简化，先删除再重建
        QueryWrapper wrapper = new QueryWrapper()
                .where("session_id", context.getSessionId());
        chatMessageService.remove(wrapper);
        
        for (SpiChatMessage msg : context.getMessages()) {
            ChatMessage entity = new ChatMessage();
            entity.setSessionId(context.getSessionId());
            entity.setRole(msg.getType().name());
            entity.setContent(msg.getContent());
            chatMessageService.save(entity);
        }
    }
}
```

---

### Task 9: 修改 BeanConfig

**文件**: `autoflow-app/src/main/java/io/autoflow/app/config/BeanConfig.java`

添加:
```java
@Bean
public DatabaseMemoryStore databaseMemoryStore(ChatSessionService chatSessionService, 
                                              ChatMessageService chatMessageService) {
    return new DatabaseMemoryStore(chatSessionService, chatMessageService);
}

@Bean
public ReActAgent reActAgent(OpenAiStreamingChatModel chatModel, 
                             ToolRegistry toolRegistry, 
                             NodeExecutor nodeExecutor,
                             DatabaseMemoryStore memoryStore) {
    return ReActAgent.builder()
            .chatModel(chatModel)
            .memoryStore(memoryStore)  // 使用DatabaseMemoryStore
            .toolRegistry(toolRegistry)
            .nodeExecutor(nodeExecutor)
            .maxSteps(10)
            .maxToolRetries(3)
            .build();
}
```

---

### Task 10: DynamicModelRegistry

**文件**: `autoflow-app/src/main/java/io/autoflow/app/config/DynamicModelRegistry.java`

```java
package io.autoflow.app.config;

import cn.hutool.json.JSONUtil;
import com.mybatisflex.core.query.QueryWrapper;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import io.autoflow.app.model.Model;
import io.autoflow.app.service.ModelService;
import io.autoflow.plugin.llm.ModelConfig;
import io.autoflow.plugin.llm.provider.ChatLanguageModelProvider;
import io.autoflow.plugin.llm.provider.ChatModelProviders;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class DynamicModelRegistry implements ModelRegistry, ApplicationRunner {
    
    private final Map<String, StreamingChatModel> models = new ConcurrentHashMap<>();
    private StreamingChatModel defaultModel;
    private final ModelService modelService;
    
    public DynamicModelRegistry(ModelService modelService, 
                                OpenAiStreamingChatModel defaultChatModel) {
        this.modelService = modelService;
        this.defaultModel = defaultChatModel;
    }
    
    @Override
    public void run(ApplicationArguments args) {
        loadModelsFromDatabase();
    }
    
    private void loadModelsFromDatabase() {
        List<Model> modelList = modelService.list();
        for (Model model : modelList) {
            try {
                StreamingChatModel chatModel = createChatModel(model);
                models.put(model.getId(), chatModel);
                log.info("Loaded model: id={}, name={}", model.getId(), model.getName());
            } catch (Exception e) {
                log.warn("Failed to load model: id={}, error={}", model.getId(), e.getMessage());
            }
        }
    }
    
    private StreamingChatModel createChatModel(Model model) {
        Map<String, Object> config = JSONUtil.toBean(model.getConfig(), Map.class);
        
        return OpenAiStreamingChatModel.builder()
                .apiKey(model.getApiKey())
                .baseUrl(model.getBaseUrl())
                .modelName((String) config.getOrDefault("modelName", "gpt-4o-mini"))
                .timeout(Duration.ofSeconds(60))
                .build();
    }
    
    @Override
    public StreamingChatModel getDefaultModel() {
        return defaultModel;
    }
    
    @Override
    public StreamingChatModel getModel(String modelId) {
        if (modelId == null || modelId.isBlank()) {
            return defaultModel;
        }
        return models.getOrDefault(modelId, defaultModel);
    }
    
    @Override
    public boolean hasModel(String modelId) {
        return modelId != null && !modelId.isBlank() && models.containsKey(modelId);
    }
    
    /**
     * 刷新模型列表 (供外部调用)
     */
    public void refreshModels() {
        models.clear();
        loadModelsFromDatabase();
    }
}
```

---

### Task 11: 修改 BeanConfig

**文件**: `autoflow-app/src/main/java/io/autoflow/app/config/BeanConfig.java`

移除/注释掉 `DefaultModelRegistry` 相关代码，因为现在使用 `DynamicModelRegistry` (它实现了 `ModelRegistry` 接口并自动注册为Bean)。

确保 `DynamicModelRegistry` 能被Spring扫描到（已添加 `@Component`）。

---

### Task 12-13: 测试验证

参考之前的测试步骤验证功能。

---

## 五、需要创建/修改的文件清单

### 新建文件
```
autoflow-app/src/main/java/io/autoflow/app/
├── model/
│   ├── ChatSession.java
│   └── ChatMessage.java
├── service/
│   ├── ChatSessionService.java
│   ├── ChatMessageService.java
│   └── impl/
│       ├── ChatSessionServiceImpl.java
│       └── ChatMessageServiceImpl.java
├── controller/ (或 rest/)
│   ├── ChatSessionController.java
│   └── ChatMessageController.java
├── query/
│   ├── ChatSessionQuery.java
│   └── ChatMessageQuery.java
└── repository/
    └── DatabaseMemoryStore.java

autoflow-app/src/main/java/io/autoflow/app/config/
    └── DynamicModelRegistry.java
```

### 修改文件
```
autoflow-app/src/main/resources/sql/schema.sql
autoflow-app/src/main/java/io/autoflow/app/config/BeanConfig.java
```

---

## 六、Success Criteria

### 验证命令
```bash
# 1. 启动后端
cd autoflow-app && mvn spring-boot:run

# 2. 创建Session
curl -X POST http://localhost:8080/chat/sessions \
  -H "Content-Type: application/json" \
  -d '{"name": "Test Chat", "modelId": "your-model-id"}'

# 3. SSE对话测试
curl -X POST http://localhost:8080/chat \
  -H "Content-Type: application/json" \
  -d '{"sessionId": "session-id", "input": "Hello"}'

# 4. 查询Session
curl http://localhost:8080/chat/sessions

# 5. 查询Message
curl "http://localhost:8080/chat/messages?sessionId=session-id"

# 6. 多轮对话验证上下文保留
# 发送多条消息到同一sessionId，验证AI记得之前的对话
```

### Final Checklist
- [ ] af_chat_session 表创建成功
- [ ] af_chat_message 表创建成功
- [ ] DatabaseMemoryStore 正确保存/加载对话
- [ ] DynamicModelRegistry 从数据库加载模型
- [ ] SSE端点返回流式响应
- [ ] 多轮对话上下文保留
- [ ] 消息持久化到数据库
