package com.digitalcopyright.service;

import org.springframework.stereotype.Service;

/**
 * @author Sakura
 */
@Service
public interface MailService {

    void sendCodeMailMessage(String email, String emailCode);

}
