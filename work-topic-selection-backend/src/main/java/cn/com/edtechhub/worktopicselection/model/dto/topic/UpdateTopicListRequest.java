package cn.com.edtechhub.worktopicselection.model.dto.topic;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

/**
 * 修改题目列表请求
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Data
public class UpdateTopicListRequest implements Serializable {

    private Long id;

    /**
     * 题目
     */
    private String topic;

    /**
     * 题目类型
     */
    private String type;

    /**
     * 题目描述
     */
    private String description;

    /**
     * 对学生要求
     */
    private String requirement;

    /**
     * 系部名
     */
    private String deptName;

    /**
     * 系部主任
     */
    private String deptTeacher;
    /**
     * 指导老师
     */
    private String teacherName;

    /// 序列化字段 ///
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}
