package cn.com.edtechhub.worktopicselection.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 获取教师题目上限请求
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Data
public class GetTeacherTopicAmountRequest implements Serializable {

    /**
     * 教师ID
     */
    private Long teacherId;

    /// 序列化字段 ///
    private static final long serialVersionUID = 1L;

}