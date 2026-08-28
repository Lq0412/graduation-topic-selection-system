package cn.com.edtechhub.worktopicselection.model.dto.topic;

import lombok.Data;

/**
 * 获取题目审核等级请求
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Data
public class GetTopicReviewLevelRequest {

    /**
     * 题目系部
     */
    private String deptName;

    /**
     * 题目主任
     */
    private String deptTeacher;

    /**
     * 题目要求
     */
    private String requirement;

    /**
     * 题目类型
     */
    private String type;

    /**
     * 题目标题
     */
    private String topic;

    /**
     * 题目描述
     */
    private String description;

}
