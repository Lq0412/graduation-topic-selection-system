package cn.com.edtechhub.worktopicselection.model.vo;

import lombok.Data;

/**
 * 系部教师脱敏类
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Data
public class DeptTeacherVO {

    /**
     * 教师名字
     */
    private String teacherName;

    /**
     * 系部名称
     */
    private String deptName;

    /**
     * 选题数量
     */
    private int topicAmount;

    /**
     * 预选人数
     */
    private int selectAmount;

    /**
     * 剩余数量
     */
    private int surplusQuantity;

}
