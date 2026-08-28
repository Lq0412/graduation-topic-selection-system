package cn.com.edtechhub.worktopicselection.model.dto.topic;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

/**
 * 题目审核请求
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Data
public class CheckTopicRequest implements Serializable {

    /**
     * 题目 id
     */
    private Long id;

    /**
     * 题目状态
     */
    private Integer status;

    /**
     * 审核理由
     */
    private String reason;

    /// 序列化字段 ///
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}