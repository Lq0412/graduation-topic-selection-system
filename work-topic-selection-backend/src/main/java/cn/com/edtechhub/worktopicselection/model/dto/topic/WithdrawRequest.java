package cn.com.edtechhub.worktopicselection.model.dto.topic;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

/**
 * 学生自行退选，或教师从自己的题目中退选指定学生。
 */
@Data
public class WithdrawRequest implements Serializable {

    private Long id;

    /**
     * 教师操作时必填；学生操作时由服务端使用当前登录账号。
     */
    private String userAccount;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
