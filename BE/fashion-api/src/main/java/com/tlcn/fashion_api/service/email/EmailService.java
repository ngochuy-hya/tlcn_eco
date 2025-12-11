package com.tlcn.fashion_api.service.email;

import java.util.Map;
import java.util.Set;

public interface EmailService {
    
    void sendVerificationEmail(String to, String name, String code);
    
    void sendInvitationEmail(String to, String inviterName, String token, Set<String> roleNames, String customMessage);
    
    void sendPasswordResetEmail(String to, String name, String code);
    
    void sendWelcomeEmail(String to, String name);
    
    void sendEmail(String to, String subject, String templateName, Map<String, Object> variables);
}
