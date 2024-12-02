package com.digitalcopyright.service;

import org.springframework.stereotype.Service;

@Service
public interface MailService {
    void sendTextMailMessage(String to, String subject, String text);

    void sendHtmlMailMessage(String to, String subject, String content);

    void sendAttachmentMailMessage(String to, String subject, String content, String filePath);

    void sendCodeMailMessage(String email, String emailCode);
}
