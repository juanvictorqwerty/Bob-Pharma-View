package com.example.backend.connection.superAdmins;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/superadmin/resend")
public class ResendController {

    @Autowired
    private sendConfirmationToken token;

    @GetMapping("/{email}")
    public String resendToken(@PathVariable String email) {
        return token.sendConfirmationToken(email);
    }
}