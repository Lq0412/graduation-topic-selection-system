package cn.com.edtechhub.worktopicselection.model.vo;

import lombok.Data;

/**
 * 选题情况脱敏类
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Data
public class SituationVO {

    /**
     * 总人数
     */
    private int Amount;

    /**
     * 已选题人数
     */
    private int selectAmount;

    /**
     * 未选题人数
     */
    private int unselectAmount;

}
