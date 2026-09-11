CREATE TABLE IF NOT EXISTS teacher_group_quota (
    teacherAccount VARCHAR(128) NOT NULL,
    groupName VARCHAR(256) NOT NULL,
    maxTopics INT NOT NULL,
    PRIMARY KEY (teacherAccount, groupName)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
