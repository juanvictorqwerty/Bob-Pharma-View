package com.example.backend.connection.admins;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.example.backend.connection.superAdmins.JwtService;

@Service
public class sendingInviteService {

    @Autowired
    private sendingInviteRepo inviteRepo;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JavaMailSender emailSender;

    @Value("${FRONTEND_URL:http://localhost:3000}")
    private String frontendUrl;

    public String sendInvite(String email) {
        sendingInviteValidation validator = new sendingInviteValidation(inviteRepo);
        validator.validate(email);

        String token = jwtService.generateToken(email, 3600 * 1000);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Invitation to join Bob-Pharma");
        message.setText(
                "Click the link to start your journey with us: " + frontendUrl + "/accept-invitation?token="
                        + token);
        emailSender.send(message);

        return "{'responseCode': 200, 'responseStatus': 'Success', 'message': 'Confirmation token sent'}";
    }
}
