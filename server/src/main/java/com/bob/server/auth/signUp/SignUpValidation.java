package com.bob.server.auth.signUp;

public enum SignUpValidation {
    
    EMAIL_REQUIRED("Email is required"),
    EMAIL_INVALID("Email should be valid"),
    PASSWORD_REQUIRED("Password is required"),
    PASSWORD_TOO_SHORT("Password must be at least 6 characters"),
    EMAIL_ALREADY_EXISTS("Email is already registered"),
    INVITE_CODE_REQUIRED("Invite code is required"),
    INVITE_CODE_INVALID("Invalid invite code"),
    INVITE_CODE_EXPIRED("Invite code has expired"),
    INVITE_CODE_ALREADY_USED("Invite code has already been used");
    
    private final String message;
    
    SignUpValidation(String message) {
        this.message = message;
    }
    
    public String getMessage() {
        return message;
    }
}