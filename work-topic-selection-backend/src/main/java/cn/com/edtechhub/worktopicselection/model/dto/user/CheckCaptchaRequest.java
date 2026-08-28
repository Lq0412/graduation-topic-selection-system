package cn.com.edtechhub.worktopicselection.model.dto.user;

import lombok.Data;

/**
 * 校验验证码请求
 */
@Data
public class CheckCaptchaRequest {

    /**
     * 请求邮箱
     */
    private String email;

    /**
     * 验证码
     */
    private String captcha;

}
