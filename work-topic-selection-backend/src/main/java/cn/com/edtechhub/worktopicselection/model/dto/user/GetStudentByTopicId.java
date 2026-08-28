package cn.com.edtechhub.worktopicselection.model.dto.user;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

/**
 * 获取学生列表
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Data
public class GetStudentByTopicId implements Serializable {

    /**
     * 题目id
     */
    private long id;

    /// 序列化字段 ///
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}