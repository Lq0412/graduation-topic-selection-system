package cn.com.edtechhub.worktopicselection.model.vo;

import lombok.Data;

/**
 * 题目选锁返回视图
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Data
public class TopicLockVO {

    /**
     * 是否锁住
     */
    Boolean islock;

    /**
     * 锁住时间
     */
    String lockTime;

}
