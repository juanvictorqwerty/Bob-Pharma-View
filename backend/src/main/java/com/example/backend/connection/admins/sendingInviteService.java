package com.example.backend.connection.admins;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.example.backend.connection.superAdmins.JwtService;

@Service
public class sendingInviteService<Invite> {

    @Autowired
    private sendingInviteRepo<Invite> inviteRepo;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JavaMailSender emailSender;

    public String sendInvite(String email) {
        sendingInviteValidation validator = new sendingInviteValidation(inviteRepo);
        validator.validate(email);

        String token = jwtService.generateToken(email, 3600);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Invitation to join Bob-Pharma");
        message.setText(
                "Click the link to start your journey with us: http://localhost:8080/api/admin/invitation/"
                        + token);
        emailSender.send(message);

        return "{'responseCode': 200, 'responseStatus': 'Success', 'message': 'Confirmation token sent'}";
    }
}
