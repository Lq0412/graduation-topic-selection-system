-- Entirely fictional demonstration data. No real accounts or credentials are included.

SET NAMES utf8mb4;

INSERT INTO `dept` (`deptName`)
SELECT '示例工程学院'
WHERE NOT EXISTS (SELECT 1 FROM `dept` WHERE `deptName` = '示例工程学院');

INSERT INTO `project` (`projectName`, `deptName`)
SELECT '示例软件工程专业', '示例工程学院'
WHERE NOT EXISTS (
    SELECT 1
    FROM `project`
    WHERE `projectName` = '示例软件工程专业'
      AND `deptName` = '示例工程学院'
);

INSERT INTO `topic` (`topic`, `type`, `description`, `requirement`, `teacherName`, `teacherAccount`, `deptName`, `deptTeacher`)
SELECT '示例：校园设备预约系统', '软件系统', '仅用于本地功能演示的虚构题目。', '掌握基础 Web 开发。', '示例教师', 'demo-teacher', '示例工程学院', '示例主任'
WHERE NOT EXISTS (SELECT 1 FROM `topic` WHERE `topic` = '示例：校园设备预约系统');
