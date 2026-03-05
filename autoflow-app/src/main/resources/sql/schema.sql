--工作流定义
CREATE TABLE IF NOT EXISTS af_workflow
(
    id            VARCHAR(32) PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    flow_str      TEXT,
    tag_ids       VARCHAR(32)[],
    plugin_ids    VARCHAR(255)[],
    description   TEXT,
    version       INTEGER   DEFAULT 1,
    creator       VARCHAR(32),
    last_modifier VARCHAR(32),
    create_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 服务插件
CREATE TABLE IF NOT EXISTS af_service
(
    id                VARCHAR(255) PRIMARY KEY,
    name              VARCHAR(255) NOT NULL,
    system            BOOLEAN   DEFAULT TRUE,
    jar_file_id       VARCHAR(32),
    description       TEXT,
    properties        JSONB,
    output_properties JSONB,
    uninstall         BOOLEAN   DEFAULT FALSE,
    creator           VARCHAR(32),
    last_modifier     VARCHAR(32),
    create_time       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 标签
CREATE TABLE IF NOT EXISTS af_tag
(
    id            VARCHAR(32) PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    creator       VARCHAR(32),
    last_modifier VARCHAR(32),
    create_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 工作流实例
CREATE TABLE IF NOT EXISTS af_workflow_inst
(
    id            VARCHAR(32) PRIMARY KEY,
    workflow_id   VARCHAR(32) NOT NULL,
    submit_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    start_time    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_time      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    duration_ms   INTEGER,
    flow_state    VARCHAR(32),
    flow_str      TEXT,
    creator       VARCHAR(32),
    last_modifier VARCHAR(32),
    create_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 执行实例
CREATE TABLE IF NOT EXISTS af_execution_inst
(
    id               VARCHAR(32) PRIMARY KEY,
    workflow_id      VARCHAR(32)  NOT NULL,
    workflow_inst_id VARCHAR(32)  NOT NULL,
    node_id          VARCHAR(32)  NOT NULL,
    service_id       VARCHAR(255) NOT NULL,
    loop_id          VARCHAR(32),
    loop_index       INTEGER,
    nr_of_instances  INTEGER,
    data             TEXT,
    start_time       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_time         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    duration_ms      INTEGER,
    error_message    TEXT,
    creator          VARCHAR(32),
    last_modifier    VARCHAR(32),
    create_time      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 全局变量
CREATE TABLE IF NOT EXISTS af_global_var
(
    id            VARCHAR(32) PRIMARY KEY,
    key           VARCHAR(255) NOT NULL,
    value         TEXT,
    creator       VARCHAR(32),
    last_modifier VARCHAR(32),
    create_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 聊天会话
CREATE TABLE IF NOT EXISTS af_chat_session
(
    id            VARCHAR(32) PRIMARY KEY,
    title         VARCHAR(255),
    status        VARCHAR(32),
    creator       VARCHAR(32),
    last_modifier VARCHAR(32),
    create_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 聊天消息
CREATE TABLE IF NOT EXISTS af_chat_message
(
    id            VARCHAR(32) PRIMARY KEY,
    session_id    VARCHAR(32) NOT NULL,
    role          VARCHAR(32) NOT NULL,
    content       TEXT,
    type          VARCHAR(32),
    metadata      TEXT,
    creator       VARCHAR(32),
    last_modifier VARCHAR(32),
    create_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 工具调用记录
CREATE TABLE IF NOT EXISTS af_tool_call
(
    id            VARCHAR(32) PRIMARY KEY,
    message_id    VARCHAR(32),
    tool_name     VARCHAR(255) NOT NULL,
    parameters    TEXT,
    result        TEXT,
    status        VARCHAR(32),
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
