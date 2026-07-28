package com.bob.server.search;

public enum SearchValidation {

    EMPTY_QUERY("Search query must not be empty"),
    INVALID_COORDINATES("Both latitude and longitude must be provided for location-based search"),
    INVALID_LATITUDE("Latitude must be between -90 and 90"),
    INVALID_LONGITUDE("Longitude must be between -180 and 180"),
    INVALID_RADIUS("Radius must be either 10, 20, or omitted for national search");

    private final String message;

    SearchValidation(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}