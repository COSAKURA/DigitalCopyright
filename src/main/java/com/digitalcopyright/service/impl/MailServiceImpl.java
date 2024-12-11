package com.digitalcopyright.service.impl;

import com.digitalcopyright.service.MailService;
import jakarta.annotation.Resource;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author Sakura
 */
@Service
@Slf4j
public class MailServiceImpl implements MailService {

    private static final Logger logger = LoggerFactory.getLogger(MailServiceImpl.class);

    @Resource
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    /**
     * 发送验证码邮件
     *
     * @param recipient 收件人邮箱
     * @param code      验证码
     */
    @Override
    public void sendCodeMailMessage(String recipient, String code) {

        // 校验参数
        if (recipient == null || recipient.trim().isEmpty()) {
            throw new IllegalArgumentException("收件人邮箱不能为空");
        }
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("验证码不能为空");
        }

        // 邮件主题
        String subject = "【DigitalCopyright】邮箱验证码";

        // 美化的 HTML 邮件内容
        String text = String.format("""
            <html>
            <head>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        background-color: #f9f9f9;
                        margin: 0;
                        padding: 0;
                    }
                    .container {
                        max-width: 600px;
                        margin: 20px auto;
                        background: #ffffff;
                        border-radius: 10px;
                        box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
                        padding: 20px;
                        text-align: center;
                    }
                    .header {
                        font-size: 24px;
                        font-weight: bold;
                        color: #4CAF50;
                    }
                    .message {
                        margin: 20px 0;
                        font-size: 16px;
                        line-height: 1.5;
                        color: #333333;
                    }
                    .code {
                        font-size: 24px;
                        font-weight: bold;
                        color: #4CAF50;
                        border: 1px dashed #4CAF50;
                        padding: 10px;
                        margin: 20px 0;
                        display: inline-block;
                        background: #f9fff9;
                    }
                    .footer {
                        margin-top: 30px;
                        font-size: 12px;
                        color: #777777;
                    }
                    .footer a {
                        color: #4CAF50;
                        text-decoration: none;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">欢迎使用 DigitalCopyright</div>
                    <div class="message">
                        您好，
                        <br>感谢您选择 DigitalCopyright 项目。我们致力于为您提供最安全的数字版权保护服务。
                        <br><br>以下是您的邮箱验证码，请在<strong>10分钟内</strong>完成验证：
                    </div>
                    <div class="code">%s</div>
                    <div class="message">
                        如果您未请求此验证码，请忽略此邮件。
                        <br>如有任何问题，请随时联系我们的客服支持团队。
                    </div>
                    <div class="footer">
                        此邮件由 DigitalCopyright 系统自动发送，请勿直接回复。
                        <br>访问我们的官网了解更多：<a href="https://www.digitalcopyright.com">https://www.digitalcopyright.com</a>
                    </div>
                </div>
            </body>
            </html>
            """, code);

        try {
            // 创建邮件消息
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            // 设置邮件属性
            helper.setFrom(senderEmail);
            helper.setTo(recipient);
            helper.setSubject(subject);
            // 设置为 HTML 格式
            helper.setText(text, true);
            helper.setSentDate(new Date());

            // 发送邮件
            mailSender.send(message);
            logger.info("验证码邮件发送成功：{} -> {}", senderEmail, recipient);

        } catch (MessagingException e) {
            logger.error("验证码邮件发送失败：{}", e.getMessage(), e);
            throw new RuntimeException("验证码邮件发送失败", e);
        }
    }
}
