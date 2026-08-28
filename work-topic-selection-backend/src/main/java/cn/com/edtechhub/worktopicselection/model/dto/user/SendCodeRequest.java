package cn.com.edtechhub.worktopicselection.model.dto.user;

import lombok.Data;

/**
 * 发送临时请求
 */
@Data
public class SendCodeRequest {

    /**
     * 用户帐号
     */
    private String userAccount;

}
