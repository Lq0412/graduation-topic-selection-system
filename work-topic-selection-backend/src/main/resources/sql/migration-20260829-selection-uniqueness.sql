-- Run this once when upgrading a database created before 2026-08-29.
-- Back up the database first.
--
-- The query below must return no rows. If it finds duplicates, resolve them
-- manually and reconcile the affected topic counters before running ALTER TABLE.
SELECT `userAccount`, `topicId`, COUNT(*) AS `duplicateCount`
FROM `student_topic_selection`
GROUP BY `userAccount`, `topicId`
HAVING COUNT(*) > 1;

-- This constraint is the database-level safety net for concurrent requests.
ALTER TABLE `student_topic_selection`
    ADD CONSTRAINT `uk_selection_user_topic` UNIQUE (`userAccount`, `topicId`);
