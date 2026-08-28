package cn.com.edtechhub.worktopicselection.service.impl;

import cn.com.edtechhub.worktopicselection.exception.CodeBindMessageEnums;
import cn.com.edtechhub.worktopicselection.service.MailService;
import cn.com.edtechhub.worktopicselection.utils.ThrowUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Resource;

/**
 * @author ljp
 */
@Service
public class MailServiceImpl implements MailService {

    @Resource
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String mailFrom;

    @Override
    public void sendSystemMail(String to, String subject, String text) {
        String escapedText = escapeHtml(text);
        String content = "<html>" +
                "<body style='font-family: Arial, sans-serif; background-color:#f5f5f5; padding:20px;'>" +
                "  <div style='max-width:600px; margin:0 auto; background:white; border-radius:8px; padding:30px; box-shadow:0 4px 10px rgba(0,0,0,0.1);'>" +
                "    <h2 style='color:#00785a; text-align:center;'>系统消息</h2>" +
                "    <p style='font-size:16px; color:#333;'>您好，感谢您使用 <b>毕业设计选题系统</b> 。</p>" +
                "    <p style='font-size:16px; color:#333;'>以下是您的系统日志: </p>" +
                "    <div style='text-align:center; margin:20px 0;'>" +
                "      <span style='display:inline-block; font-size:28px; font-weight:bold; color:#fff; background:#00785a; padding:10px 20px; border-radius:6px;'>" + escapedText + "</span>" +
                "    </div>" +
                "    <p style='font-size:14px; color:#666;'>请重点关注本系统邮件，注意防范！</p>" +
                "    <hr style='margin:30px 0; border:none; border-top:1px solid #ddd;'/>" +
                "    <p style='font-size:12px; color:#999; text-align:center;'>此邮件由系统自动发送，请不要直接回复。</p>" +
                "  </div>" +
                "</body>" +
                "</html>";

        try {
            javax.mail.internet.MimeMessage message = mailSender.createMimeMessage();
            org.springframework.mail.javamail.MimeMessageHelper helper = new org.springframework.mail.javamail.MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailFrom);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(message);
        } catch (javax.mail.MessagingException e) {
            ThrowUtils.throwIf(true, CodeBindMessageEnums.SYSTEM_ERROR, "发送邮件失败，请联系系统管理员");
        }
    }

    @Override
    public void sendCodeMail(String to, String subject, String text) {
        String escapedText = escapeHtml(text);
        String content = "<html>" +
                "<body style='font-family: Arial, sans-serif; background-color:#f5f5f5; padding:20px;'>" +
                "  <div style='max-width:600px; margin:0 auto; background:white; border-radius:8px; padding:30px; box-shadow:0 4px 10px rgba(0,0,0,0.1);'>" +
                "    <h2 style='color:#00785a; text-align:center;'>系统消息</h2>" +
                "    <p style='font-size:16px; color:#333;'>您好，感谢您使用 <b>毕业设计选题系统</b> 。</p>" +
                "    <p style='font-size:16px; color:#333;'>以下是您的临时密码: </p>" +
                "    <div style='text-align:center; margin:20px 0;'>" +
                "      <span style='display:inline-block; font-size:28px; font-weight:bold; color:#fff; background:#00785a; padding:10px 20px; border-radius:6px;'>" + escapedText + "</span>" +
                "    </div>" +
                "    <p style='font-size:14px; color:#666;'>临时密码有效期为 2 分钟，请勿泄露给他人。</p>" +
                "    <hr style='margin:30px 0; border:none; border-top:1px solid #ddd;'/>" +
                "    <p style='font-size:12px; color:#999; text-align:center;'>此邮件由系统自动发送，请不要直接回复。</p>" +
                "  </div>" +
                "</body>" +
                "</html>";

        try {
            javax.mail.internet.MimeMessage message = mailSender.createMimeMessage();
            org.springframework.mail.javamail.MimeMessageHelper helper = new org.springframework.mail.javamail.MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailFrom);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(message);
        } catch (javax.mail.MessagingException e) {
            ThrowUtils.throwIf(true, CodeBindMessageEnums.SYSTEM_ERROR, "发送邮件失败，请联系系统管理员");
        }
    }

    @Override
    public void sendCaptchaMail(String to, String subject, String text) {
        String escapedText = escapeHtml(text);
        String content = "<html>" +
                "<body style='font-family: Arial, sans-serif; background-color:#f5f5f5; padding:20px;'>" +
                "  <div style='max-width:600px; margin:0 auto; background:white; border-radius:8px; padding:30px; box-shadow:0 4px 10px rgba(0,0,0,0.1);'>" +
                "    <h2 style='color:#00785a; text-align:center;'>验证消息</h2>" +
                "    <p style='font-size:16px; color:#333;'>您好，感谢您使用 <b>毕业设计选题系统</b> 。</p>" +
                "    <p style='font-size:16px; color:#333;'>以下是您的验证码: </p>" +
                "    <div style='text-align:center; margin:20px 0;'>" +
                "      <span style='display:inline-block; font-size:28px; font-weight:bold; color:#fff; background:#00785a; padding:10px 20px; border-radius:6px;'>" + escapedText + "</span>" +
                "    </div>" +
                "    <p style='font-size:14px; color:#666;'>验证码有效期为 2 分钟，请勿泄露给他人。</p>" +
                "    <hr style='margin:30px 0; border:none; border-top:1px solid #ddd;'/>" +
                "    <p style='font-size:12px; color:#999; text-align:center;'>此邮件由系统自动发送，请不要直接回复。</p>" +
                "  </div>" +
                "</body>" +
                "</html>";

        try {
            javax.mail.internet.MimeMessage message = mailSender.createMimeMessage();
            org.springframework.mail.javamail.MimeMessageHelper helper = new org.springframework.mail.javamail.MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailFrom);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(message);
        } catch (javax.mail.MessagingException e) {
            ThrowUtils.throwIf(true, CodeBindMessageEnums.SYSTEM_ERROR, "发送邮件失败，请联系系统管理员");
        }
    }

    @Override
    public void sendReasonMail(String to, String subject, String text) {
        String escapedText = escapeHtml(text);
        String content = "<html>" +
                "<body style='font-family: Arial, sans-serif; background-color:#f5f5f5; padding:20px;'>" +
                "  <div style='max-width:600px; margin:0 auto; background:white; border-radius:8px; padding:30px; box-shadow:0 4px 10px rgba(0,0,0,0.1);'>" +
                "    <h2 style='color:#00785a; text-align:center;'>打回消息</h2>" +
                "    <p style='font-size:16px; color:#333;'>您好，感谢您使用 <b>毕业设计选题系统</b> 。</p>" +
                "    <p style='font-size:16px; color:#333;'>您有题目被打回，打回理由为: </p>" +
                "    <div style='text-align:center; margin:20px 0;'>" +
                "      <span style='display:inline-block; font-size:28px; font-weight:bold; color:#fff; background:#00785a; padding:10px 20px; border-radius:6px;'>" + escapedText + "</span>" +
                "    </div>" +
                "    <p style='font-size:14px; color:#666;'>请及时处理并重新提交审核，避免拖延进程，如果遇到无法处理的问题请联系主任或管理员。</p>" +
                "    <hr style='margin:30px 0; border:none; border-top:1px solid #ddd;'/>" +
                "    <p style='font-size:12px; color:#999; text-align:center;'>此邮件由系统自动发送，请不要直接回复。</p>" +
                "  </div>" +
                "</body>" +
                "</html>";

        try {
            javax.mail.internet.MimeMessage message = mailSender.createMimeMessage();
            org.springframework.mail.javamail.MimeMessageHelper helper = new org.springframework.mail.javamail.MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailFrom);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(message);
        } catch (javax.mail.MessagingException e) {
            ThrowUtils.throwIf(true, CodeBindMessageEnums.SYSTEM_ERROR, "发送邮件失败，请联系系统管理员");
        }
    }

    @Override
    public void sendTopicMail(String to, String subject, String text) {
        String escapedText = escapeHtml(text);
        String content = "<html>" +
                "<body style='font-family: Arial, sans-serif; background-color:#f5f5f5; padding:20px;'>" +
                "  <div style='max-width:600px; margin:0 auto; background:white; border-radius:8px; padding:30px; box-shadow:0 4px 10px rgba(0,0,0,0.1);'>" +
                "    <h2 style='color:#00785a; text-align:center;'>退选消息</h2>" +
                "    <p style='font-size:16px; color:#333;'>您好，感谢您使用 <b>毕业设计选题系统</b> 。</p>" +
                "    <p style='font-size:16px; color:#333;'>您有题目被确认退选，操作信息为: </p>" +
                "    <div style='text-align:center; margin:20px 0;'>" +
                "      <span style='display:inline-block; font-size:28px; font-weight:bold; color:#fff; background:#00785a; padding:10px 20px; border-radius:6px;'>" + escapedText + "</span>" +
                "    </div>" +
                "    <p style='font-size:14px; color:#666;'>请确认退选是否自愿操作或导师操作，如果遇到无法处理的问题请联系主任或管理员。</p>" +
                "    <hr style='margin:30px 0; border:none; border-top:1px solid #ddd;'/>" +
                "    <p style='font-size:12px; color:#999; text-align:center;'>此邮件由系统自动发送，请不要直接回复。</p>" +
                "  </div>" +
                "</body>" +
                "</html>";

        try {
            javax.mail.internet.MimeMessage message = mailSender.createMimeMessage();
            org.springframework.mail.javamail.MimeMessageHelper helper = new org.springframework.mail.javamail.MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailFrom);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(message);
        } catch (javax.mail.MessagingException e) {
            ThrowUtils.throwIf(true, CodeBindMessageEnums.SYSTEM_ERROR, "发送邮件失败，请联系系统管理员");
        }
    }

    static String escapeHtml(String text) {
        return HtmlUtils.htmlEscape(text == null ? "" : text);
    }

}
