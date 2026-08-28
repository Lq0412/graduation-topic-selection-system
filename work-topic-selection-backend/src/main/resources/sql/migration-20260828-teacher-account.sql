-- Run this once when upgrading an existing database created before 2026-08-28.
-- Back up the database first.

ALTER TABLE `topic`
    ADD COLUMN `teacherAccount` VARCHAR(256) DEFAULT NULL COMMENT '指导老师账号（归属校验）'
        AFTER `teacherName`;

-- Only backfill names that identify exactly one active teacher in the same department.
UPDATE `topic` AS t
JOIN (
    SELECT `userName`, `dept`, MIN(`userAccount`) AS `teacherAccount`
    FROM `user`
    WHERE `userRole` = 1 AND `isDelete` = 0
    GROUP BY `userName`, `dept`
    HAVING COUNT(*) = 1
) AS u
  ON u.`userName` = t.`teacherName`
 AND u.`dept` = t.`deptName`
SET t.`teacherAccount` = u.`teacherAccount`
WHERE t.`teacherAccount` IS NULL;

CREATE INDEX `idx_topic_teacher_account` ON `topic` (`teacherAccount`);

-- This query must return no active topics before teachers can manage every legacy topic.
-- Resolve ambiguous or missing matches explicitly; the application deliberately refuses
-- name-only ownership checks because same-name teachers would otherwise be able to act
-- on one another's topics.
SELECT `id`, `topic`, `teacherName`, `deptName`
FROM `topic`
WHERE `isDelete` = 0 AND `teacherAccount` IS NULL;
