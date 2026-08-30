-- Public, non-destructive MySQL 8 schema.
-- Create the work_topic_selection database and database user separately.

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `user`
(
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'id',
    `userAccount`  VARCHAR(128) NOT NULL COMMENT '账号',
    `userName`     VARCHAR(256)          DEFAULT NULL COMMENT '用户姓名',
    `userPassword` VARCHAR(512) NOT NULL COMMENT '密码散列',
    `createTime`   DATETIME              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime`   DATETIME              DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete`     TINYINT      NOT NULL DEFAULT 0 COMMENT '是否删除',
    `userRole`     INT          NOT NULL DEFAULT 0 COMMENT '用户角色 0-学生 1-教师 2-主任 3-系统',
    `dept`         VARCHAR(256)          DEFAULT NULL COMMENT '系部',
    `status`       VARCHAR(256)          DEFAULT NULL COMMENT '账号状态',
    `project`      VARCHAR(256)          DEFAULT NULL COMMENT '专业',
    `topicAmount`  INT                   DEFAULT NULL COMMENT '预先选题数量/最大出题数量',
    `email`        VARCHAR(256)          DEFAULT NULL COMMENT '验证码发送邮箱',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_account` (`userAccount`),
    KEY `idx_user_name` (`userName`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '用户表';

CREATE TABLE IF NOT EXISTS `dept`
(
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'id',
    `deptName`   VARCHAR(256) NOT NULL COMMENT '系部名',
    `createTime` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete`   TINYINT      NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY `idx_dept_name` (`deptName`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '系部表';

CREATE TABLE IF NOT EXISTS `project`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'id',
    `projectName` VARCHAR(256) NOT NULL COMMENT '专业名',
    `createTime`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete`    TINYINT      NOT NULL DEFAULT 0 COMMENT '是否删除',
    `deptName`    VARCHAR(256)          DEFAULT NULL COMMENT '系部名',
    `groupName`   VARCHAR(256)          DEFAULT NULL COMMENT '选题组',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '专业表';

CREATE TABLE IF NOT EXISTS `topic`
(
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'id',
    `topic`           VARCHAR(255)           DEFAULT NULL COMMENT '题目',
    `type`            VARCHAR(255)           DEFAULT NULL COMMENT '题目类型',
    `description`     LONGTEXT COMMENT '题目描述',
    `requirement`     LONGTEXT COMMENT '对学生要求',
    `teacherName`     VARCHAR(256)           DEFAULT NULL COMMENT '指导老师',
    `teacherAccount`  VARCHAR(256)           DEFAULT NULL COMMENT '指导老师账号（归属校验）',
    `deptName`        VARCHAR(256)           DEFAULT NULL COMMENT '系部名',
    `deptTeacher`     VARCHAR(256)           DEFAULT NULL COMMENT '系部主任',
    `topicGroup`     VARCHAR(256)           DEFAULT NULL COMMENT '适用选题组',
    `createTime`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete`        TINYINT       NOT NULL DEFAULT 0 COMMENT '是否删除',
    `surplusQuantity` INT           NOT NULL DEFAULT 1 COMMENT '剩余数量',
    `startTime`       DATETIME               DEFAULT NULL COMMENT '开启时间',
    `endTime`         DATETIME               DEFAULT NULL COMMENT '结束时间',
    `status`          INT           NOT NULL DEFAULT -1 COMMENT '状态: -2-打回 -1-待审核 0-未发布 1-已发布',
    `selectAmount`    INT                    DEFAULT 0 COMMENT '预选人数',
    `reason`          VARCHAR(256)           DEFAULT NULL COMMENT '打回理由',
    PRIMARY KEY (`id`),
    KEY `idx_topic_teacher_account` (`teacherAccount`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '选题表';

CREATE TABLE IF NOT EXISTS `student_topic_selection`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'id',
    `userAccount` VARCHAR(256) NOT NULL COMMENT '账号',
    `topicId`     BIGINT       NOT NULL COMMENT '题目 id',
    `createTime`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete`    TINYINT      NOT NULL DEFAULT 0 COMMENT '是否删除',
    `status`      INT          NOT NULL DEFAULT 0 COMMENT '选题状态: -1-取消预选 0-确认预选 1-取消选题 2-确认选题',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_selection_user_topic` (`userAccount`, `topicId`),
    KEY `idx_selection_user_account` (`userAccount`),
    KEY `idx_selection_topic_id` (`topicId`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '用户选题关联表';
