package cn.com.edtechhub.worktopicselection.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 设置教师题目上限请求
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Data
public class SetTeacherTopicAmountRequest implements Serializable {

    /**
     * 教师ID
     */
    private Long teacherId;

    /**
     * 题目上限数量
     */
    private Integer topicAmount;

    /// 序列化字段 ///
    private static final long serialVersionUID = 1L;

}