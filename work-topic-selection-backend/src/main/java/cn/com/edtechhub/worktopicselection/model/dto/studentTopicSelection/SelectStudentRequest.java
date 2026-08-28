package cn.com.edtechhub.worktopicselection.model.dto.studentTopicSelection;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

/**
 * 选择学生请求
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Data
public class SelectStudentRequest implements Serializable {

    /**
     * 学生账号
     */
    private String userAccount;

    /**
     * 题目
     */
    private String topic;

    /// 序列化字段 ///
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}