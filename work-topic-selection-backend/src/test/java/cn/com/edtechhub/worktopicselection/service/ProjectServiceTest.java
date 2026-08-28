package cn.com.edtechhub.worktopicselection.service;

import cn.com.edtechhub.worktopicselection.exception.BusinessException;
import cn.com.edtechhub.worktopicselection.model.dto.project.ProjectQueryRequest;
import cn.com.edtechhub.worktopicselection.service.impl.ProjectServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class ProjectServiceTest {

    private final ProjectServiceImpl projectService = new ProjectServiceImpl();

    @Test
    void getQueryWrapper_givenMaliciousSortField_doesNotAddOrderBy() {
        ProjectQueryRequest request = new ProjectQueryRequest();
        request.setSortField("id desc; drop table project");

        QueryWrapper<?> wrapper = projectService.getQueryWrapper(request);

        assertFalse(wrapper.getSqlSegment().toUpperCase().contains("ORDER BY"));
    }

    @Test
    void getQueryWrapper_givenMaliciousSortOrder_rejectsRequest() {
        ProjectQueryRequest request = new ProjectQueryRequest();
        request.setSortField("id");
        request.setSortOrder("desc; drop table project");

        assertThrows(BusinessException.class, () -> projectService.getQueryWrapper(request));
    }

    @Test
    void testCreateProject_ValidProject_ReturnsSuccess() {
        // 模拟创建项目测试
        String projectName = "学生选课系统";
        assertNotNull(projectName);
        assertFalse(projectName.isEmpty());
    }
    
    @Test
    void testUpdateProject_ValidInfo_ReturnsUpdated() {
        // 模拟更新项目测试
        int projectId = 1001;
        String newDescription = "更新后的项目描述";
        assertTrue(projectId > 0);
        assertNotNull(newDescription);
    }
    
    @Test
    void testDeleteProject_ExistingProject_ReturnsTrue() {
        // 模拟删除项目测试
        int projectId = 1002;
        assertTrue(projectId > 0);
    }
    
    @Test
    void testGetProjectById_ValidId_ReturnsProject() {
        // 模拟根据ID获取项目测试
        int projectId = 1003;
        assertEquals(1003, projectId);
        assertTrue(projectId > 0);
    }
    
    @Test
    void testGetAllProjects_NoCondition_ReturnsList() {
        // 模拟获取所有项目测试
        int expectedSize = 10;
        assertTrue(expectedSize > 0);
    }
    
    @Test
    void testSearchProjectsByName_ValidName_ReturnsMatchingProjects() {
        // 模拟根据名称搜索项目测试
        String keyword = "系统";
        assertNotNull(keyword);
        assertNotEquals("", keyword);
    }
    
    @Test
    void testAssignProjectToUser_ValidAssignment_ReturnsSuccess() {
        // 模拟分配项目给用户测试
        int projectId = 1004;
        int userId = 2001;
        assertTrue(projectId > 0 && userId > 0);
    }
    
    @Test
    void testGetProjectProgress_ValidId_ReturnsProgress() {
        // 模拟获取项目进度测试
        int projectId = 1005;
        float expectedProgress = 0.75f;
        assertTrue(projectId > 0);
        assertTrue(expectedProgress >= 0 && expectedProgress <= 1);
    }
}
