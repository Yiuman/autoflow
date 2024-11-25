--工作流定义
CREATE TABLE IF NOT EXISTS af_workflow
(
    id            VARCHAR(32) PRIMARY KEY,             -- 主键，UUID 类型，默认值自动生成
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

CREATE TABLE IF NOT EXISTS af_service
(
    id            VARCHAR(255) PRIMARY KEY,            -- 主键，UUID 类型，默认值自动生成
    name          VARCHAR(255) NOT NULL,               -- 插件名称
    system        BOOLEAN   DEFAULT TRUE,              -- 是否为系统插件
    jar_file_id   VARCHAR(32),                         -- jar包文件ID
    description   TEXT,                                -- 描述字段
    properties    JSONB,                               --  参数属性
    output_type   JSONB,                               -- 输出类型
    uninstall     BOOLEAN   DEFAULT FALSE,             -- 是否已卸载
    creator       VARCHAR(32),                         -- 创建者 ID，使用 UUID 类型
    last_modifier VARCHAR(32),                         -- 最后修改者 ID，使用 UUID 类型
    create_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 创建时间，默认当前时间
    update_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP  -- 更新时间，默认当前时间
);
-- 标签
CREATE TABLE IF NOT EXISTS af_tag
(
    id            VARCHAR(32) PRIMARY KEY,             -- 主键，UUID 类型，默认值自动生成
    name          VARCHAR(255) NOT NULL,               -- 不为空的工作流名称
    creator       VARCHAR(32),                         -- 创建者 ID，使用 UUID 类型
    last_modifier VARCHAR(32),                         -- 最后修改者 ID，使用 UUID 类型
    create_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 创建时间，默认当前时间
    update_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP  -- 更新时间，默认当前时间
);
-- 工作流实例
CREATE TABLE IF NOT EXISTS af_workflow_inst
(
    id            VARCHAR(32) PRIMARY KEY,             -- 主键，UUID 类型，默认值自动生成
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
    id               VARCHAR(32) PRIMARY KEY,               -- 主键，UUID 类型，默认值自动生成
    workflow_id      VARCHAR(32)  NOT NULL,                 -- 工作流定义主键
    workflow_inst_id VARCHAR(32)  NOT NULL,                 -- 工作流实例主键
    node_id          VARCHAR(32)  NOT NULL,                 -- 节点ID
    service_id       VARCHAR(255) NOT NULL,                 -- 服务节点ID
    loop_id          VARCHAR(32),                           -- 循环ID
    loop_counter     INTEGER,                               -- 循环次数
    nr_of_instances  INTEGER,                               -- 循环实例总数
    data             TEXT,                                  -- 数据
    start_time       TIMESTAMP   DEFAULT CURRENT_TIMESTAMP, -- 开始时间
    end_time         TIMESTAMP   DEFAULT CURRENT_TIMESTAMP, -- 结束时间
    duration_ms      INTEGER,                               -- 耗时（毫秒）
    error_message    TEXT,                                  --错误信息
    flow_state       VARCHAR(32) DEFAULT CURRENT_TIMESTAMP, -- 耗时（毫秒）
    flow_str         TEXT,                                  -- 存储 JSON 数据，推荐使用 JSONB 类型
    creator          VARCHAR(32),                           -- 创建者 ID，使用 UUID 类型
    last_modifier    VARCHAR(32),                           -- 最后修改者 ID，使用 UUID 类型
    create_time      TIMESTAMP   DEFAULT CURRENT_TIMESTAMP, -- 创建时间，默认当前时间
    update_time      TIMESTAMP   DEFAULT CURRENT_TIMESTAMP  -- 更新时间，默认当前时间
);