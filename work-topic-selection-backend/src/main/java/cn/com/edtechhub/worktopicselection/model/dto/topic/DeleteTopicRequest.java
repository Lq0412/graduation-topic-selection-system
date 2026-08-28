package cn.com.edtechhub.worktopicselection.model.dto.topic;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

/**
 * 删除题目请求
 * 
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Data
public class DeleteTopicRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /// 序列化字段 ///
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}