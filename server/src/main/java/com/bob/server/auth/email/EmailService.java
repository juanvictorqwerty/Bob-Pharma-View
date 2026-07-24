package com.bob.server.auth.email;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.bob.server.model.Code;
import com.bob.server.repositories.CodeRepository;
import com.bob.server.repositories.UsersRepository;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    
    private final CodeRepository codeRepository;
    private final UsersRepository usersRepository;
    private final JavaMailSender mailSender;
    
    public EmailService(CodeRepository codeRepository, UsersRepository usersRepository, JavaMailSender mailSender) {
        this.codeRepository = codeRepository;
        this.usersRepository = usersRepository;
        this.mailSender = mailSender;
    }
    
    public Code createInvite(EmailDTO inviteDTO) {
        // Check if current user is authenticated and has Admin role
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("You must be logged in to create invites");
        }
        
        // Check if user has Admin role
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_Admin"));
        
        if (!isAdmin) {
            throw new SecurityException("Only admins can create invite codes");
        }
        
        String email = inviteDTO.getEmail();
        String category = inviteDTO.getCategory();
        
        if (category == null || !category.trim().equalsIgnoreCase("Admin")) {
            throw new IllegalArgumentException("Category must be Admin");
        }
        
        if (usersRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already registered as a user");
        }
        
        String inviteCode = generateInviteCode();
        
        Code code = new Code();
        code.setCategory("Admin");
        code.setEmail(email);
        code.setCode(inviteCode);
        code.setUsed(false);
        code.setCreatedAt(Instant.now());
        code.setExpiresAt(Instant.now().plus(4, java.time.temporal.ChronoUnit.HOURS));
        
        Code savedCode = codeRepository.save(code);
        
        // Send invite email
        sendInviteEmail(email, inviteCode);
        
        return savedCode;
    }
    
    public Code createResetPasswordCode(String email) {
        // Check if user exists
        if (!usersRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email not found");
        }
        
        // Invalidate any existing unused reset codes for this email
        codeRepository.findByEmailAndCategoryAndUsedFalse(email, "RESET_PASSWORD")
            .ifPresent(code -> {
                code.setUsed(true);
                codeRepository.save(code);
            });
        
        String resetCode = generateResetCode();
        
        Code code = new Code();
        code.setCategory("RESET_PASSWORD");
        code.setEmail(email);
        code.setCode(resetCode);
        code.setUsed(false);
        code.setCreatedAt(Instant.now());
        code.setExpiresAt(Instant.now().plus(4, java.time.temporal.ChronoUnit.HOURS));
        
        Code savedCode = codeRepository.save(code);
        
        // Send reset password email
        sendResetPasswordEmail(email, resetCode);
        
        return savedCode;
    }
    
    public Code verifyCode(String email, String code, String category) {
        Code codeEntity = codeRepository.findByEmailAndCodeAndCategory(email, code, category)
            .orElseThrow(() -> new IllegalArgumentException("Invalid code"));
        
        if (codeEntity.isUsed()) {
            throw new IllegalStateException("Code has already been used");
        }
        
        if (codeEntity.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalStateException("Code has expired");
        }
        
        return codeEntity;
    }
    
    public void markCodeAsUsed(Code code) {
        code.setUsed(true);
        codeRepository.save(code);
    }
    
    private String generateInviteCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
    
    private String generateResetCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
    
    private void sendInviteEmail(String toEmail, String inviteCode) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom("noreply@bobpharma.com");
            helper.setTo(toEmail);
            helper.setSubject("You're Invited to Join Bob Pharma");
            
            String htmlContent = loadTemplate("templates/invite-email.html", inviteCode);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new MailSendException("Failed to send invite email: " + e.getMessage());
        }
    }
    
    private void sendResetPasswordEmail(String toEmail, String resetCode) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom("noreply@bobpharma.com");
            helper.setTo(toEmail);
            helper.setSubject("Reset Your Password - Bob Pharma");
            
            String htmlContent = loadTemplate("templates/reset-password-email.html", resetCode);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new MailSendException("Failed to send reset password email: " + e.getMessage());
        }
    }
    
    private String loadTemplate(String templatePath, String code) {
        try {
            ClassPathResource resource = new ClassPathResource(templatePath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
            String template = reader.lines().collect(Collectors.joining("\n"));
            reader.close();
            
            // Replace placeholder with actual code
            if (templatePath.contains("invite")) {
                return template.replace("{{inviteCode}}", code);
            } else {
                return template.replace("{{resetCode}}", code);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load email template: " + e.getMessage());
        }
    }
}
