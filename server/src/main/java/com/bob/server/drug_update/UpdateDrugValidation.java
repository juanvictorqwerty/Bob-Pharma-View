package com.bob.server.drug_update;

public enum UpdateDrugValidation {

    FILE_EMPTY("Uploaded file is empty"),
    INVALID_FORMAT("Invalid file format. Only .xlsx files are accepted"),
    MISSING_COLUMNS("Excel file must contain 'name' and 'quantity' columns"),
    INVALID_HEADERS("Excel headers are invalid or missing"),
    PARSE_ERROR("Failed to parse the Excel file"),
    BATCH_FAILED("Batch update failed due to a row error"),
    PHARMACY_NOT_FOUND("Pharmacy not found"),
    ACCESS_DENIED("You do not have access to this pharmacy"),
    INVALID_QUANTITY("Quantity must be a positive number");

    private final String message;

    UpdateDrugValidation(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}