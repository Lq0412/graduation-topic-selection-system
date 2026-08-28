package cn.com.edtechhub.worktopicselection.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户脱敏类
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Data
public class UserVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar = "/logo_128.png";

    /**
     * 用户角色：user/admin/ban
     */
    private Integer userRole;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 状态
     */
    private String status;

}
