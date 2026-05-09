package com.example.backend.connection.superAdmins;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class sendConfirmationToken {

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private SuperCreationRepo superCreationRepo;

    @Autowired
    private JwtService jwtService;

    public String sendConfirmationToken(String email) {

        if (!superCreationRepo.existsById(email)) {
            return "{'responseCode': 400, 'responseStatus': 'Error', 'message': 'Email does not exist'}";
        }

        String token = jwtService.generateToken(email);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Confirmation Token");
        message.setText(
                "Click the link to confirm your email: http://localhost:8080/api/superadmin/confirmation/" + token);
        emailSender.send(message);

        return "{'responseCode': 200, 'responseStatus': 'Success', 'message': 'Confirmation token sent'}";
    }
}
