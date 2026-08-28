package cn.com.edtechhub.worktopicselection.model.dto.user;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户创建请求
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Data
public class ResetPasswordRequest implements Serializable {

    /**
     * 用户姓名
     */
    private String userName;

    /**
     * 账号
     */
    private String userAccount;

    /// 序列化字段 ///
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}
