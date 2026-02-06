package com.uniqlm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;
    @Value("${app.reset-password-url}")
    private String resetPasswordBaseUrl;

    @Value("${app.verify-email-url}")
    private String verifyEmailBaseUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendEmail(String to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);

        mailSender.send(message);
    }

    public void sendPasswordResetEmail(String to, String token) {
        String resetUrl = resetPasswordBaseUrl + "?token=" + token;
        String message = "To reset your password, click the link below:\n" + resetUrl;

        sendEmail(to, "Password Reset Request", message);
    }

    public void sendVerificationEmail(String to, String token) {
        String verifyUrl = verifyEmailBaseUrl + "?token=" + token;
        String message = "Welcome to UniQLM!\n\n" +
                "Please verify your email address by clicking the link below:\n" +
                verifyUrl + "\n\n" +
                "This link will expire in 24 hours.\n\n" +
                "If you didn't create an account with UniQLM, you can safely ignore this email.";

        sendEmail(to, "Verify your UniQLM account", message);
    }
}