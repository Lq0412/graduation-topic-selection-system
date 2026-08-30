-- 专业分组选题迁移（MySQL 8）
-- 仅增加可为空字段，不修改和删除现有数据；重复执行前请确认字段已存在。

SET @project_group_column_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'project'
      AND column_name = 'groupName'
);
SET @project_group_sql = IF(
    @project_group_column_exists = 0,
    'ALTER TABLE `project` ADD COLUMN `groupName` VARCHAR(256) NULL COMMENT ''选题组'' AFTER `deptName`',
    'SELECT 1'
);
PREPARE project_group_stmt FROM @project_group_sql;
EXECUTE project_group_stmt;
DEALLOCATE PREPARE project_group_stmt;

SET @topic_group_column_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'topic'
      AND column_name = 'topicGroup'
);
SET @topic_group_sql = IF(
    @topic_group_column_exists = 0,
    'ALTER TABLE `topic` ADD COLUMN `topicGroup` VARCHAR(256) NULL COMMENT ''适用选题组'' AFTER `deptTeacher`',
    'SELECT 1'
);
PREPARE topic_group_stmt FROM @topic_group_sql;
EXECUTE topic_group_stmt;
DEALLOCATE PREPARE topic_group_stmt;
