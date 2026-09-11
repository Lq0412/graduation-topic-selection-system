package cn.com.edtechhub.worktopicselection.service;

import cn.com.edtechhub.worktopicselection.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;

class TeacherGroupServiceTest {
    @Test
    void quotasAreIndependentAndDeletedTopicsDoNotConsumeCapacity() {
        JdbcTemplate db = new JdbcTemplate(new DriverManagerDataSource("jdbc:h2:mem:quota;MODE=MySQL", "sa", ""));
        // Keep this connection open for the lifetime of this isolated database.
        try (java.sql.Connection connection = db.getDataSource().getConnection()) {
            db.execute("CREATE TABLE teacher_group_quota(teacherAccount VARCHAR,groupName VARCHAR,maxTopics INT)");
            db.execute("CREATE TABLE topic(id BIGINT,teacherAccount VARCHAR,topicGroup VARCHAR,isDelete INT)");
            db.update("INSERT INTO teacher_group_quota VALUES ('T1','第一组',3),('T1','第三组',5)");
            db.update("INSERT INTO topic VALUES (1,'T1','第一组',0),(2,'T1','第一组',0),(3,'T1','第一组',0),(4,'T1','第三组',1)");
            TeacherGroupService service = new TeacherGroupService();
            ReflectionTestUtils.setField(service,"jdbcTemplate",db);
            assertThrows(BusinessException.class, () -> service.validate("T1","第一组",null));
            assertDoesNotThrow(() -> service.validate("T1","第三组",null));
            assertDoesNotThrow(() -> service.validate("T1","第一组",1L));
            assertThrows(BusinessException.class, () -> service.validate("T1","第二组",null));
            assertThrows(BusinessException.class, () -> service.validate("T1",null,null));
            assertThrows(BusinessException.class, () -> service.validate("T2","第一组",null));
        } catch (java.sql.SQLException e) {
            throw new AssertionError(e);
        }
    }
}
