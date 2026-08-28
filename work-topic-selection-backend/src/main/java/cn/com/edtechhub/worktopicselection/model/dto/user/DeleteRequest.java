package cn.com.edtechhub.worktopicselection.model.dto.user;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户删除请求
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Data
public class DeleteRequest implements Serializable {

    /**
     * 账号(因为用户账号本身就是唯一的, 因此完全可以替代 id 值)
     */
    private String userAccount;

    /// 序列化字段 ///
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}