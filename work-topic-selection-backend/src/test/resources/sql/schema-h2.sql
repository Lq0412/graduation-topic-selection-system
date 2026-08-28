CREATE TABLE IF NOT EXISTS `user`
(
    `id`           BIGINT AUTO_INCREMENT PRIMARY KEY,
    `userAccount`  VARCHAR(128) NOT NULL UNIQUE,
    `userName`     VARCHAR(256),
    `userPassword` VARCHAR(512) NOT NULL,
    `createTime`   DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updateTime`   DATETIME DEFAULT CURRENT_TIMESTAMP,
    `isDelete`     TINYINT  DEFAULT 0 NOT NULL,
    `userRole`     INT      DEFAULT 0 NOT NULL,
    `dept`         VARCHAR(256),
    `status`       VARCHAR(256),
    `project`      VARCHAR(256),
    `topicAmount`  INT,
    `email`        VARCHAR(256)
);

CREATE INDEX IF NOT EXISTS `idx_user_name` ON `user` (`userName`);

CREATE TABLE IF NOT EXISTS `dept`
(
    `id`         BIGINT AUTO_INCREMENT PRIMARY KEY,
    `deptName`   VARCHAR(256) NOT NULL,
    `createTime` DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    `updateTime` DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    `isDelete`   TINYINT  DEFAULT 0 NOT NULL
);

CREATE INDEX IF NOT EXISTS `idx_dept_name` ON `dept` (`deptName`);

CREATE TABLE IF NOT EXISTS `project`
(
    `id`          BIGINT AUTO_INCREMENT PRIMARY KEY,
    `projectName` VARCHAR(256) NOT NULL,
    `createTime`  DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    `updateTime`  DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    `isDelete`    TINYINT  DEFAULT 0 NOT NULL,
    `deptName`    VARCHAR(256)
);

CREATE TABLE IF NOT EXISTS `topic`
(
    `id`              BIGINT AUTO_INCREMENT PRIMARY KEY,
    `topic`           VARCHAR(255),
    `type`            VARCHAR(255),
    `description`     LONGTEXT,
    `requirement`     LONGTEXT,
    `teacherName`     VARCHAR(256),
    `teacherAccount`  VARCHAR(256),
    `deptName`        VARCHAR(256),
    `deptTeacher`     VARCHAR(256),
    `createTime`      DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    `updateTime`      DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    `isDelete`        TINYINT  DEFAULT 0 NOT NULL,
    `surplusQuantity` INT      DEFAULT 1 NOT NULL,
    `startTime`       DATETIME,
    `endTime`         DATETIME,
    `status`          INT      DEFAULT -1 NOT NULL,
    `selectAmount`    INT      DEFAULT 0,
    `reason`          VARCHAR(256)
);

CREATE INDEX IF NOT EXISTS `idx_topic_teacher_account` ON `topic` (`teacherAccount`);

CREATE TABLE IF NOT EXISTS `student_topic_selection`
(
    `id`          BIGINT AUTO_INCREMENT PRIMARY KEY,
    `userAccount` VARCHAR(256) NOT NULL,
    `topicId`     BIGINT       NOT NULL,
    `createTime`  DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    `updateTime`  DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    `isDelete`    TINYINT  DEFAULT 0 NOT NULL,
    `status`      INT      DEFAULT 0 NOT NULL
);

CREATE INDEX IF NOT EXISTS `idx_selection_user_account` ON `student_topic_selection` (`userAccount`);
CREATE INDEX IF NOT EXISTS `idx_selection_topic_id` ON `student_topic_selection` (`topicId`);
CREATE UNIQUE INDEX IF NOT EXISTS `uk_selection_user_topic` ON `student_topic_selection` (`userAccount`, `topicId`);
