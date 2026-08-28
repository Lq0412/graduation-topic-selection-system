package cn.com.edtechhub.worktopicselection.model.dto.user;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

/**
 * 管理员请求用户数据
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Data
public class GetUserListRequest implements Serializable {

    /**
     * 用户角色 0 - 普通用户 1 - 教师 2 - 系部 3 - 管理员
     */
    private Integer userRole;

    /// 序列化字段 ///
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}