CREATE TABLE IF NOT EXISTS `AF_WORKFLOW`
(
    `id`            varchar(32) PRIMARY KEY COMMENT '主键ID',
    `name`          varchar(2000) DEFAULT NULL COMMENT '流程名称',
    `flowStr`       text          DEFAULT NULL COMMENT '流程的json字符串',
    `desc`          text          DEFAULT NULL COMMENT '描述',
    `creator`       varchar(32) NOT NULL COMMENT '创建者',
    `last_modifier` VARCHAR(32)   DEFAULT NULL COMMENT '最后的更新人',
    `create_time`   datetime      DEFAULT NULL COMMENT '创建时间',
    `update_time`   datetime      DEFAULT NULL COMMENT '最后的更新时间'
);


CREATE TABLE IF NOT EXISTS `AF_WORKFLOW`
(
    `id`            varchar(32) PRIMARY KEY COMMENT '主键ID',
    `name`          varchar(2000) DEFAULT NULL COMMENT '流程名称',
    `flowStr`       text          DEFAULT NULL COMMENT '流程的json字符串',
    `desc`          text          DEFAULT NULL COMMENT '描述',
    `creator`       varchar(32) NOT NULL COMMENT '创建者',
    `last_modifier` VARCHAR(32)   DEFAULT NULL COMMENT '最后的更新人',
    `create_time`   datetime      DEFAULT NULL COMMENT '创建时间',
    `update_time`   datetime      DEFAULT NULL COMMENT '最后的更新时间'
);



