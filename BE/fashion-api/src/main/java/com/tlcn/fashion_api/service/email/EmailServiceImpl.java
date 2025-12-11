package com.tlcn.fashion_api.service.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {
    
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    
    @Value("${app.email.from-name}")
    private String fromName;
    
    @Value("${app.email.from-address}")
    private String fromAddress;
    
    @Value("${app.verification.email-base-url}")
    private String verificationBaseUrl;
    
    @Value("${app.invitation.base-url}")
    private String invitationBaseUrl;
    
    @Value("${app.password-reset.base-url}")
    private String passwordResetBaseUrl;
    
    @Override
    @Async
    public void sendVerificationEmail(String to, String name, String code) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", name);
        variables.put("code", code);
        variables.put("verificationUrl", verificationBaseUrl + "?code=" + code);
        
        sendEmail(to, "Verify Your Email - " + fromName, "verification-email", variables);
    }
    
    @Override
    @Async
    public void sendInvitationEmail(String to, String inviterName, String token, Set<String> roleNames, String customMessage) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("inviterName", inviterName);
        variables.put("roles", roleNames);
        variables.put("invitationUrl", invitationBaseUrl + "?token=" + token);
        variables.put("customMessage", customMessage);
        variables.put("shopName", fromName);
        
        sendEmail(to, "You're invited to join " + fromName, "invitation-email", variables);
    }
    
    @Override
    @Async
    public void sendPasswordResetEmail(String to, String name, String code) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", name);
        variables.put("code", code);
        variables.put("resetUrl", passwordResetBaseUrl + "?code=" + code);
        
        sendEmail(to, "Reset Your Password - " + fromName, "password-reset-email", variables);
    }
    
    @Override
    @Async
    public void sendWelcomeEmail(String to, String name) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", name);
        variables.put("shopName", fromName);
        
        sendEmail(to, "Welcome to " + fromName, "welcome-email", variables);
    }
    
    @Override
    public void sendEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            Context context = new Context();
            context.setVariables(variables);
            
            String htmlContent = templateEngine.process(templateName, context);
            
            helper.setFrom(fromAddress, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        } catch (Exception e) {
            log.error("Unexpected error while sending email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
