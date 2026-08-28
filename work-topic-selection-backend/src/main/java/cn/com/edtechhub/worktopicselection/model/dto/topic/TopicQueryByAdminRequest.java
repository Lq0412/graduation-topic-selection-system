package cn.com.edtechhub.worktopicselection.model.dto.topic;

import cn.com.edtechhub.worktopicselection.model.dto.PageRequest;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户查询请求
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TopicQueryByAdminRequest extends PageRequest implements Serializable {

    /**
     * 题目
     */
    private String topic;

    /**
     * 题目类型
     */
    private String type;

    /**
     * 指导老师
     */
    private String teacherName;

    /**
     * 系部名
     */
    private String deptName;

    /**
     * 开启时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /// 序列化字段 ///
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}