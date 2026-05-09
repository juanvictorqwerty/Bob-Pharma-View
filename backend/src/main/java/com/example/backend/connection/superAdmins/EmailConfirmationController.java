package com.example.backend.connection.superAdmins;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/superadmin/confirmation")
public class EmailConfirmationController {

    @Autowired
    private emailConfirmationService confirmationService;

    @GetMapping("/{token}")
    public String confirmEmail(@PathVariable String token) {
        return confirmationService.confirmCode(token);
    }
}
