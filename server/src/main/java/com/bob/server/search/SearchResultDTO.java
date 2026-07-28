package com.bob.server.search;

import java.util.List;
import java.util.UUID;

public class SearchResultDTO {

    private String type;
    private int page;
    private int size;
    private long totalResults;
    private int totalPages;
    private List<SearchItem> results;
    private List<NationalGroup> groups;

    public SearchResultDTO() {}

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public long getTotalResults() { return totalResults; }
    public void setTotalResults(long totalResults) { this.totalResults = totalResults; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public List<SearchItem> getResults() { return results; }
    public void setResults(List<SearchItem> results) { this.results = results; }

    public List<NationalGroup> getGroups() { return groups; }
    public void setGroups(List<NationalGroup> groups) { this.groups = groups; }

    public static class SearchItem {
        private UUID pharmacyId;
        private String pharmacyName;
        private String region;
        private String city;
        private Double latitude;
        private Double longitude;
        private Double distanceKm;
        private UUID drugId;
        private String drugName;
        private int quantity;
        private double score;
        private String stockUpdatedAt;

        public SearchItem() {}

        public UUID getPharmacyId() { return pharmacyId; }
        public void setPharmacyId(UUID pharmacyId) { this.pharmacyId = pharmacyId; }
        public String getPharmacyName() { return pharmacyName; }
        public void setPharmacyName(String pharmacyName) { this.pharmacyName = pharmacyName; }
        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public Double getLatitude() { return latitude; }
        public void setLatitude(Double latitude) { this.latitude = latitude; }
        public Double getLongitude() { return longitude; }
        public void setLongitude(Double longitude) { this.longitude = longitude; }
        public Double getDistanceKm() { return distanceKm; }
        public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }
        public UUID getDrugId() { return drugId; }
        public void setDrugId(UUID drugId) { this.drugId = drugId; }
        public String getDrugName() { return drugName; }
        public void setDrugName(String drugName) { this.drugName = drugName; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public double getScore() { return score; }
        public void setScore(double score) { this.score = score; }
        public String getStockUpdatedAt() { return stockUpdatedAt; }
        public void setStockUpdatedAt(String stockUpdatedAt) { this.stockUpdatedAt = stockUpdatedAt; }
    }

    public static class NationalGroup {
        private String region;
        private List<CityGroup> cities;

        public NationalGroup() {}

        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }
        public List<CityGroup> getCities() { return cities; }
        public void setCities(List<CityGroup> cities) { this.cities = cities; }
    }

    public static class CityGroup {
        private String city;
        private List<SearchItem> pharmacies;

        public CityGroup() {}

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public List<SearchItem> getPharmacies() { return pharmacies; }
        public void setPharmacies(List<SearchItem> pharmacies) { this.pharmacies = pharmacies; }
    }
}