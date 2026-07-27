package com.bob.server.drug_update;

public class UpdateDrugException extends RuntimeException {

    private final UpdateDrugValidation validation;

    public UpdateDrugException(UpdateDrugValidation validation) {
        super(validation.getMessage());
        this.validation = validation;
    }

    public UpdateDrugValidation getValidation() {
        return validation;
    }
}