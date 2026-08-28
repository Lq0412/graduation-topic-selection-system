package cn.com.edtechhub.worktopicselection.model.dto.topic;

import cn.com.edtechhub.worktopicselection.model.dto.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户查询请求
 *
 *
 * @author Lzh
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TopicQueryRequest extends PageRequest implements Serializable {

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

    /**
     * 状态
     */
    private Integer status;

    /**
     * 是否获取没人选的题目
     */
    private Boolean isNoOneSelectedTopic;

    /// 序列化字段 ///
    private static final long serialVersionUID = 1L;

}
