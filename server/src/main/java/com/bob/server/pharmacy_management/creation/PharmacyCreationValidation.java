package com.bob.server.pharmacy_management.creation;

public enum PharmacyCreationValidation {

    PHARMACY_ALREADY_EXISTS("A pharmacy with this name already exists in this region and city"),
    PHARMACY_NOT_FOUND("Pharmacy not found"),
    PHARMACY_NOT_APPROVED("Pharmacy is not approved yet"),
    PHARMACY_ALREADY_APPROVED("Pharmacy is already approved"),
    PHARMACY_SUSPENDED("Pharmacy is suspended"),
    UNAUTHORIZED_ACTION("You are not authorized to perform this action"),
    USER_NOT_FOUND("User not found"),
    USER_ALREADY_STAFF("User is already a staff member of this pharmacy"),
    STAFF_NOT_FOUND("Staff member not found"),
    INVALID_ROLE("Invalid role. Must be PHARMACY_ADMIN or PHARMACY_PERSONNEL"),
    CANNOT_REMOVE_CREATOR("Cannot remove the pharmacy creator from staff"),
    PHARMACY_NOT_ACTIVE("Pharmacy is not active"),
    STAFF_ALREADY_SUSPENDED("Staff member is already suspended"),
    STAFF_NOT_SUSPENDED("Staff member is not suspended"),
    CANNOT_SUSPEND_CREATOR("Cannot suspend the pharmacy creator");

    private final String message;

    PharmacyCreationValidation(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}