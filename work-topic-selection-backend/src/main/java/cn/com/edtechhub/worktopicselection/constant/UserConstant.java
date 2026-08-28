package cn.com.edtechhub.worktopicselection.constant;

/**
 * 用户常量
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
public interface UserConstant {

    int MAX_USER_ACCOUNT_LENGTH = 128;

    /**
     * 旧版 MD5 密码使用的盐，仅用于兼容登录并迁移到 BCrypt。
     */
    String LEGACY_PASSWORD_SALT = "edtechhub";

    /**
     * 用户登录态键
     */
    String USER_LOGIN_STATE = "user_login";

    String VERIFIED_EMAIL_SESSION_KEY = "verified_email";

    String VERIFIED_EMAIL_EXPIRES_AT_SESSION_KEY = "verified_email_expires_at";

}
