CREATE TABLE IF NOT EXISTS `AF_FLOW_DEF`
(
    `id`                     varchar(32) PRIMARY KEY COMMENT '主键ID',
    `name`                   varchar(2000) DEFAULT NULL COMMENT '流程名称',
    `username`               text          DEFAULT NULL COMMENT '流程的json字符串',
    `process_definition_id`  varchar(64)   DEFAULT NULL COMMENT 'flowable流程定义的ID',
    `process_definition_key` varchar(64)   DEFAULT NULL COMMENT 'flowable流程定义的KEY',
    `creator`                varchar(32) NOT NULL COMMENT '创建者',
    `last_modifier`          VARCHAR(32)   DEFAULT NULL COMMENT '最后的更新人',
    `create_time`            datetime      DEFAULT NULL COMMENT '创建时间',
    `update_time`            datetime      DEFAULT NULL COMMENT '最后的更新时间'
);