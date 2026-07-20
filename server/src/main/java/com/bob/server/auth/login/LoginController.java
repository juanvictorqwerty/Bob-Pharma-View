package com.bob.server.auth.login;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping("api/login")
    public ResponseEntity<LoginResponse> postMethodName(@RequestBody LoginRequest request) {
        LoginResponse response = loginService.login(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
