package com.example.backend.connection.superAdmins;

public class ApiResponse {

    private int responseCode;
    private String responseStatus;
    private String message;

    public ApiResponse() {
    }

    public ApiResponse(int responseCode, String responseStatus, String message) {
        this.responseCode = responseCode;
        this.responseStatus = responseStatus;
        this.message = message;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(String responseStatus) {
        this.responseStatus = responseStatus;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
