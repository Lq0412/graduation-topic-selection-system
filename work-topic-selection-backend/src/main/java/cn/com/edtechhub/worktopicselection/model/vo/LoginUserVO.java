package cn.com.edtechhub.worktopicselection.model.vo;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 登录用户脱敏类
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Data
public class LoginUserVO implements Serializable {

    /**
     * 用户 id
     */
    private Long id;

    /**
     * 用户名字
     */
    private String userName;

    /**
     * 用户角色：user/admin/ban
     */
    private Integer userRole;

    /**
     * 用户系部
     */
    private String dept;

    /**
     * 用户专业
     */
    private String project;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 用户头像
     */
    private String userAvatar = "/logo_128.png";

    /**
     * 用户邮箱
     */
    private String email;

    private static final long serialVersionUID = 1L;
}
