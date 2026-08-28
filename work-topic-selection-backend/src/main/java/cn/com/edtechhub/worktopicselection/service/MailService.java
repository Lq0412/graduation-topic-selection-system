package cn.com.edtechhub.worktopicselection.service;

/**
 * 邮箱服务接口
 *
 * @author ljp
 */
public interface MailService {

    /**
     * 发送系统邮件
     *
     * @param to      收件人邮箱
     * @param subject 邮件主题
     * @param text    邮件内容
     */
    void sendSystemMail(String to, String subject, String text);

    /**
     * 发送临时密码邮箱
     *
     * @param to      收件人邮箱
     * @param subject 邮件主题
     * @param text 邮件内容
     */
    void sendCodeMail(String to, String subject, String text);

    /**
     * 发送验证码邮箱
     *
     * @param to      收件人邮箱
     * @param subject 邮件主题
     * @param text 邮件内容
     */
    void sendCaptchaMail(String to, String subject, String text);

    /**
     * 发送题目打回邮件
     *
     * @param to      收件人邮箱
     * @param subject 邮件主题
     * @param text    邮件内容
     */
    void sendReasonMail(String to, String subject, String text);

    /**
     * 发送题目退选邮件
     *
     * @param to      收件人邮箱
     * @param subject 邮件主题
     * @param text    邮件内容
     */
    void sendTopicMail(String to, String subject, String text);

}
