package cn.com.edtechhub.worktopicselection.model.dto.user;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

/**
 * 用户注册请求体
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Data
public class UserUpdatePassword implements Serializable {

    /**
     * 用户账号
     */
    private String userAccount;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 旧的密码
     */
    private String userPassword;

    /**
     * 新的密码
     */
    private String updatePassword;

    /**
     * 临时密码
     */
    private String code;

    /// 序列化字段 ///
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}
