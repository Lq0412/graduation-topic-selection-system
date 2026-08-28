package cn.com.edtechhub.worktopicselection.model.vo;

import cn.com.edtechhub.worktopicselection.model.entity.User;
import lombok.Data;

import java.util.List;

/**
 * 系统信息视图
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Data
public class TheSystemInfoVO {

    /// 选题信息 ///

    /**
     * 当前主任数量
     */
    private Long totalDeptCount;

    /**
     * 当前教师数量
     */
    private Long totalTeacherCount;

    /**
     * 当前学生数量
     */
    private Long totalStudentCount;

    /**
     * 完全没有登陆过系统的用户数量
     */
    private Long loginUserCount;

    /**
     * 当前系统审核通过的题目数量
     */
    private Long auditPassTopicCount;

    /**
     * 当前系统审核打回的题目数量
     */
    private Long auditBackTopicCount;

    /**
     * 当前系统审核待审的题目数量
     */
    private Long auditTopicCount;

    /**
     * 当前系统处于发布的题目数量
     */
    private Long releaseTopicCount;

    /// 系统信息 ///

    /**
     * 当前系统使用内存资源
     */
    private String memoryUsage;

    /**
     * 当前系统使用 CPU 资源
     */
    private String cpuUsage;

    /**
     * 当前系统使用硬盘资源
     */
    private String diskUsage;

    /**
     * 当前 JVM 虚拟内存资源
     */
    private String jvmMemoryUsage;

}
