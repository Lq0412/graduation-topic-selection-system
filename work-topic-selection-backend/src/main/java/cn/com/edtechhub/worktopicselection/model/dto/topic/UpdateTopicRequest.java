package cn.com.edtechhub.worktopicselection.model.dto.topic;

import cn.com.edtechhub.worktopicselection.model.dto.PageRequest;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 修改题目请求
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Data
public class UpdateTopicRequest implements Serializable {

    /**
     * 需要修改的题目名字
     */
    private String topicName;

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

    /// 序列化字段 ///
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}
