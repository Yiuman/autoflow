--工作流定义
CREATE TABLE IF NOT EXISTS af_workflow
(
    id
                  VARCHAR(32) PRIMARY KEY,             -- 主键，UUID 类型，默认值自动生成
    name          VARCHAR(255) NOT NULL,               -- 不为空的工作流名称
    flow_str      TEXT,                                -- 存储 JSON 数据，推荐使用 JSONB 类型
    tag_ids       VARCHAR(32)[],                       -- UUID 数组，存储标签 ID 集合
    plugin_ids    VARCHAR(255)[],                      -- UUID 数组，存储插件 ID 集合
    description   TEXT,                                -- 描述字段
    version       INTEGER   DEFAULT 1,                 -- 默认版本号为 1
    creator       VARCHAR(32),                         -- 创建者 ID，使用 UUID 类型
    last_modifier VARCHAR(32),                         -- 最后修改者 ID，使用 UUID 类型
    create_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 创建时间，默认当前时间
    update_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP  -- 更新时间，默认当前时间
);
-- 服务插件
CREATE TABLE IF NOT EXISTS af_service
(
    id
                      VARCHAR(255) PRIMARY KEY,            -- 主键，UUID 类型，默认值自动生成
    name              VARCHAR(255) NOT NULL,               -- 插件名称
    system            BOOLEAN   DEFAULT TRUE,              -- 是否为系统插件
    jar_file_id       VARCHAR(32),                         -- jar包文件ID
    description       TEXT,                                -- 描述字段
    properties        JSONB,                               --  参数属性
    output_properties JSONB,                               -- 输出类型
    uninstall         BOOLEAN   DEFAULT FALSE,             -- 是否已卸载
    creator           VARCHAR(32),                         -- 创建者 ID，使用 UUID 类型
    last_modifier     VARCHAR(32),                         -- 最后修改者 ID，使用 UUID 类型
    create_time       TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 创建时间，默认当前时间
    update_time       TIMESTAMP DEFAULT CURRENT_TIMESTAMP  -- 更新时间，默认当前时间
);
-- 标签
CREATE TABLE IF NOT EXISTS af_tag
(
    id
                  VARCHAR(32) PRIMARY KEY,             -- 主键，UUID 类型，默认值自动生成
    name          VARCHAR(255) NOT NULL,               -- 不为空的工作流名称
    creator       VARCHAR(32),                         -- 创建者 ID，使用 UUID 类型
    last_modifier VARCHAR(32),                         -- 最后修改者 ID，使用 UUID 类型
    create_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 创建时间，默认当前时间
    update_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP  -- 更新时间，默认当前时间
);
-- 工作流实例
CREATE TABLE IF NOT EXISTS af_workflow_inst
(
    id
                  VARCHAR(32) PRIMARY KEY,             -- 主键，UUID 类型，默认值自动生成
    workflow_id   VARCHAR(32) NOT NULL,                -- 工作流定义主键
    submit_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 提交时间
    start_time    TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 开始时间
    end_time      TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 结束时间
    duration_ms   INTEGER,                             -- 耗时（毫秒）
    flow_state    VARCHAR(32),                         -- 状态
    flow_str      TEXT,                                -- 存储 JSON 数据，推荐使用 JSONB 类型
    creator       VARCHAR(32),                         -- 创建者 ID，使用 UUID 类型
    last_modifier VARCHAR(32),                         -- 最后修改者 ID，使用 UUID 类型
    create_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 创建时间，默认当前时间
    update_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP  -- 更新时间，默认当前时间
);
-- 执行实例
CREATE TABLE IF NOT EXISTS af_execution_inst
(
    id
                     VARCHAR(32) PRIMARY KEY,             -- 主键，UUID 类型，默认值自动生成
    workflow_id      VARCHAR(32)  NOT NULL,               -- 工作流定义主键
    workflow_inst_id VARCHAR(32)  NOT NULL,               -- 工作流实例主键
    node_id          VARCHAR(32)  NOT NULL,               -- 节点ID
    service_id       VARCHAR(255) NOT NULL,               -- 服务节点ID
    loop_id          VARCHAR(32),                         -- 循环ID
    loop_index       INTEGER,                             -- 循环次数
    nr_of_instances  INTEGER,                             -- 循环实例总数
    data             TEXT,                                -- 数据
    start_time       TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 开始时间
    end_time         TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 结束时间
    duration_ms      INTEGER,                             -- 耗时（毫秒）
    error_message    TEXT,                                --错误信息
    creator          VARCHAR(32),                         -- 创建者 ID，使用 UUID 类型
    last_modifier    VARCHAR(32),                         -- 最后修改者 ID，使用 UUID 类型
    create_time      TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 创建时间，默认当前时间
    update_time      TIMESTAMP DEFAULT CURRENT_TIMESTAMP  -- 更新时间，默认当前时间
);

-- 全局变量
CREATE TABLE IF NOT EXISTS af_global_var
(
    id
                  VARCHAR(32) PRIMARY KEY,             -- 主键，UUID 类型，默认值自动生成
    key           VARCHAR(255) not null,               -- 变量key
    value         TEXT,                                -- 变量值
    creator       VARCHAR(32),                         -- 创建者 ID，使用 UUID 类型
    last_modifier VARCHAR(32),                         -- 最后修改者 ID，使用 UUID 类型
    create_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 创建时间，默认当前时间
    update_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP  -- 更新时间，默认当前时间
);
-- AI 模型配置
CREATE TABLE IF NOT EXISTS af_model
(
    id
                  VARCHAR(32) PRIMARY KEY,             -- 主键，UUID 类型
    name          VARCHAR(255) NOT NULL,               -- 模型名称
    base_url      VARCHAR(512),                        -- API 基础 URL
    api_key       VARCHAR(512),                        -- API 密钥
    config        TEXT,                                -- 模型配置 (JSON 格式)
    creator       VARCHAR(32),                         -- 创建者 ID，使用 UUID 类型
    last_modifier VARCHAR(32),                         -- 最后修改者 ID，使用 UUID 类型
    create_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 创建时间，默认当前时间
    update_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP  -- 更新时间，默认当前时间
);

-- Chat Session
CREATE TABLE IF NOT EXISTS af_chat_session
(
    id
                  VARCHAR(32) PRIMARY KEY,
    title         VARCHAR(255),
    model_id      VARCHAR(32),
    system_prompt TEXT,
    status        VARCHAR(32) DEFAULT 'ACTIVE',
    creator       VARCHAR(32),
    last_modifier VARCHAR(32),
    create_time   TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    update_time   TIMESTAMP   DEFAULT CURRENT_TIMESTAMP
);

-- Chat Message
CREATE TABLE IF NOT EXISTS af_chat_message
(
    id
                     VARCHAR(32) PRIMARY KEY,
    session_id       VARCHAR(32) NOT NULL,
    role             VARCHAR(32) NOT NULL,
    content          TEXT,
    thinking_content TEXT,
    metadata         TEXT,
    creator          VARCHAR(32),
    last_modifier    VARCHAR(32),
    create_time      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY
        (
         session_id
            ) REFERENCES af_chat_session
        (
         id
            ) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_message_session ON af_chat_message (session_id);

-- File Resource
CREATE TABLE IF NOT EXISTS af_file
(
    id            VARCHAR(32) PRIMARY KEY,
    filename      VARCHAR(255),
    size          BIGINT,
    path          VARCHAR(512),
    metadata      TEXT,
    platform      VARCHAR(64),
    content_type  VARCHAR(128),
    creator       VARCHAR(32),
    last_modifier VARCHAR(32),
    create_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Agent Config
CREATE TABLE IF NOT EXISTS af_agent_config
(
    id             VARCHAR(32) PRIMARY KEY,
    name           VARCHAR(255) NOT NULL,
    system_prompt  TEXT,
    max_steps      INTEGER,
    max_tool_retries INTEGER,
    tool_ids       TEXT,
    creator        VARCHAR(32),
    last_modifier  VARCHAR(32),
    create_time    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 初始化 Agent Config 数据
-- 默认通用助手
INSERT INTO af_agent_config (id, name, system_prompt, max_steps, max_tool_retries, tool_ids, creator, last_modifier, create_time, update_time)
VALUES (
    'default',
    '通用助手',
    'You are a helpful AI assistant with access to tools.

## Guidelines
1. Think step-by-step before taking action - use Thought to reason through the problem
2. Use tools only when necessary - if you know the answer, respond directly
3. When a tool fails, acknowledge the error, reflect on what went wrong, and try alternative approaches
4. Be concise but thorough in your reasoning

## Response Format
When using tools, follow this format:

Question: {user_question}
Thought: [Describe your reasoning]
Action: [Tool name from available tools]
Action Input: [Arguments in JSON format]
Observation: [Result will appear here]
... (Thought/Action/Observation can repeat as needed)

Thought: Based on my reasoning and observations, I now have the answer.
Final Answer: [Your concise response]

## Important
- When you have completed the task, provide your Final Answer',
    10,
    3,
    NULL,
    NULL,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    system_prompt = EXCLUDED.system_prompt,
    max_steps = EXCLUDED.max_steps,
    max_tool_retries = EXCLUDED.max_tool_retries;

-- 工作流设计助手
INSERT INTO af_agent_config (id, name, system_prompt, max_steps, max_tool_retries, tool_ids, creator, last_modifier, create_time, update_time)
VALUES (
    'workflow-designer',
    '工作流设计助手',
    'You are a professional workflow design expert. Your task is to understand user requirements and design executable workflows.

## 工作流程
1. **问题理解**: 充分理解用户想要自动化什么任务，明确输入和输出
2. **步骤拆解**: 将任务分解为具体的执行步骤，每个步骤对应一个节点
3. **节点选择**: 确定每个步骤使用哪个 Service 节点
4. **流程设计**: 确定节点的执行顺序，添加适当的条件分支和循环
5. **数据传递**: 使用表达式配置节点间的数据引用
6. **输出工作流**: 调用 AutoFlowDesigner 工具生成工作流

## Response Format
在设计工作流时，按以下格式思考：

Thought: [详细分析 - 用户想要完成什么？需要哪些步骤？每个步骤的输入输出是什么？节点间如何传递数据？]
... (如需了解可用节点，可调用相应工具)
Thought: [基于以上分析，现在开始生成工作流]
Action: AutoFlowDesigner
Action Input: {"workflowJson": "<工作流JSON>"}

## Workflow JSON Format
```json
{
  "name": "工作流名称",
  "description": "工作流描述",
  "nodes": [
    {
      "id": "简洁英文ID，如 http_req、query_db",
      "type": "SERVICE|IF|LOOP_EACH_ITEM",
      "serviceId": "服务实现类ID",
      "label": "节点显示名称",
      "data": {}
    }
  ],
  "connections": [
    {
      "source": "节点ID",
      "target": "节点ID",
      "sourcePointType": "出口类型（如需要）",
      "targetPointType": "入口类型（如需要）",
      "expression": "可选的条件表达式"
    }
  ]
}
```

### 正确示例
nodes: `[{"id": "upload", ...}, {"id": "process", ...}, {"id": "save", ...}]`
connections: `[{"source": "upload", "target": "process"}, {"source": "process", "target": "save"}]`

### 关键规则
1. **每个节点的 id 必须唯一**
2. **connections 中的 source 和 target 必须是 nodes 中已定义的 id**
3. **不要在 connections 中使用 nodes 中不存在的 id**
4. **serviceId 必须是系统中真实存在的服务类 ID**

## Node Types
- **SERVICE**: 服务执行节点，调用具体的 Service 完成特定任务
- **IF**: 条件判断节点，用于分支路由
- **LOOP_EACH_ITEM**: 循环节点，用于遍历集合数据

## 连接类型（Connection sourcePointType/targetPointType）

### ⚠️ 核心规则（必须遵守）
1. **sourcePointType 只允许 IF 和 LOOP_EACH_ITEM 节点使用**
2. **❌ 普通节点（SERVICE等）绝对禁止写 sourcePointType，写了就是错误 ❌**
3. **❌ LOOP_DONE 禁止指向循环节点本身（避免闭环）❌**

### 出口类型
| sourcePointType | 适用节点 | 说明 |
|---------|---------|------|
| `OUTPUT` | 所有节点 | 默认出口 |
| `IF_TRUE` | IF节点 | 条件为真时的出口 |
| `IF_FALSE` | IF节点 | 条件为假时的出口 |
| `LOOP_EACH` | LOOP_EACH_ITEM节点 | 遍历每个元素时的出口 |
| `LOOP_DONE` | LOOP_EACH_ITEM节点 | 遍历结束后的出口 |

### 入口类型
| targetPointType | 说明 |
|---------|------|
| `INPUT` | 默认入口 |

### 正确示例
```json
// 普通节点连接（绝对不要写sourcePointType）
{"source": "upload", "target": "process"}
{"source": "process", "target": "save"}

// IF 节点连接
{"source": "check", "target": "handle_true", "sourcePointType": "IF_TRUE"}
{"source": "check", "target": "handle_false", "sourcePointType": "IF_FALSE"}

// LOOP 节点连接
{"source": "loop", "target": "process_item", "sourcePointType": "LOOP_EACH"}
{"source": "loop", "target": "finish", "sourcePointType": "LOOP_DONE"}
```

### ❌ 绝对禁止的错误
1. **普通节点（SERVICE）连接禁止写 sourcePointType**
   ```json
   // 错误：save 是普通节点，不能写 sourcePointType ❌
   {"source": "save", "target": "upload", "sourcePointType": "LOOP_DONE"}
   // 错误：search_cases 是普通节点，不能写 sourcePointType ❌
   {"source": "search_cases", "target": "xxx", "sourcePointType": "LOOP_DONE"}
   ```

2. **禁止LOOP_DONE指向循环节点本身（闭环）**
   ```json
   // 错误：LOOP_DONE 指向 loop_node 造成闭环 ❌
   {"source": "collect", "target": "loop_node", "sourcePointType": "LOOP_DONE"}
   ```

## 节点数据引用语法
每个节点的输出以其 nodeId 作为 key 存储在执行上下文中：

| 语法 | 示例 | 说明 |
|------|------|------|
| ${nodeId} | ${http_request_1} | 引用整个节点的输出 |
| ${nodeId.field} | ${http_request_1.data.name} | 从节点输出中提取字段 |
| $.nodeId.path | $.http_request_1.data.items[0] | 使用 JSONPath 提取 |

## 设计原则
1. 每个 SERVICE 节点职责单一，只做一件事
2. IF 节点必须连接两个分支：IF_TRUE 和 IF_FALSE
3. LOOP_EACH_ITEM 节点必须连接两个出口：LOOP_EACH（遍历中）和 LOOP_DONE（结束后）
4. 工作流应该清晰无环，避免死循环
5. 节点间的数据引用要正确配置，确保数据能正确传递
6. 考虑错误处理，必要时添加异常处理节点
7. **节点 id 使用简洁英文，如 http_req、query_db、send_email**
8. **connections 中的 source/target 必须与 nodes 中的 id 完全一致**

## Important
- 先进行 Thought 分析，明确工作流结构后再调用 AutoFlowDesigner
- 使用可用节点来构建工作流，不要编造不存在的节点或 serviceId
- **serviceId 必须是系统中真实存在的服务类 ID，格式如 io.autoflow.plugin.http.HttpRequestService**
- **connections 中的 source/target 必须与 nodes 中的 id 完全一致**
- **IF 和 LOOP_EACH_ITEM 节点必须指定正确的 sourcePointType（IF_TRUE/IF_FALSE/LOOP_EACH/LOOP_DONE）**
- workflowJson 必须是合法的 JSON 格式',
    15,
    3,
    NULL,
    NULL,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    system_prompt = EXCLUDED.system_prompt,
    max_steps = EXCLUDED.max_steps,
    max_tool_retries = EXCLUDED.max_tool_retries;