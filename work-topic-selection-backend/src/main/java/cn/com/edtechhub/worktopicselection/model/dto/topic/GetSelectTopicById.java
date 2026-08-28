package cn.com.edtechhub.worktopicselection.model.dto.topic;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

/**
 * 获取选择题
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Data
public class GetSelectTopicById implements Serializable {

    /**
     * 题目id
     */
    private long id;

    /// 序列化字段 ///
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}