package com.bob.server.search;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class SearchRequest {

    @NotBlank(message = "Drug name query is required")
    private String query;

    private Double latitude;

    private Double longitude;

    @Min(value = 1, message = "Radius must be at least 1")
    @Max(value = 20000, message = "Radius must not exceed 20000")
    private Integer radiusKm;

    @Min(value = 0, message = "Page must be non-negative")
    private int page = 0;

    @Min(value = 1, message = "Size must be at least 1")
    @Max(value = 50, message = "Size must not exceed 50")
    private int size = 10;

    public SearchRequest() {}

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Integer getRadiusKm() { return radiusKm; }
    public void setRadiusKm(Integer radiusKm) { this.radiusKm = radiusKm; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public boolean hasCoordinates() {
        return latitude != null && longitude != null;
    }
}