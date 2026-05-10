package com.example.backend.connection.superAdmins;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.example.backend.models.Users;

@Service
public class sendConfirmationTokenService {

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private SuperCreationRepo superCreationRepo;

    @Autowired
    private JwtService jwtService;

    public String sendConfirmationToken(String email) {
        Users user = superCreationRepo.findByEmail(email).orElse(null);
        if (user == null) {
            return "{'responseCode': 400, 'responseStatus': 'Error', 'message': 'Email does not exist'}";
        }
        if (user.isVerified() == true) {
            return "{'responseCode': 400, 'responseStatus': 'Error', 'message': 'Email already verified'}";
        }

        String token = jwtService.generateToken(email, 3600000);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Confirmation Token");
        message.setText(
                "Click the link to confirm your email: http://localhost:8080/api/superadmin/confirmation/" + token);
        emailSender.send(message);

        return "{'responseCode': 200, 'responseStatus': 'Success', 'message': 'Confirmation token sent'}";
    }
}
