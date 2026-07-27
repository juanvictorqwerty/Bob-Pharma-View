package com.bob.server.search;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.bob.server.repositories.StockRepository;

@Service
public class SearchService {

    private static final double DEFAULT_RADIUS_KM = 10.0;
    private static final double EXPANDED_RADIUS_KM = 20.0;
    private static final double NATIONAL_RADIUS_KM = 20000.0; // far enough to cover any country
    private static final double EARTH_RADIUS_KM = 6371.0;

    private final StockRepository stockRepository;

    public SearchService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    public SearchResultDTO search(SearchRequest request) {
        String query = request.getQuery().trim().toLowerCase();

        if (request.hasCoordinates()) {
            double latitude = request.getLatitude();
            double longitude = request.getLongitude();
            double radiusKm = resolveRadius(request.getRadiusKm());
            double radiusMeters = radiusKm * 1000.0;

            List<Object[]> rawResults = stockRepository.searchDrugsGeo(query, latitude, longitude, radiusMeters);

            List<SearchResultDTO.SearchItem> items = mapGeoResults(rawResults, latitude, longitude);
            items = rankAndSort(items);

            return buildGeoResponse(items, request.getPage(), request.getSize());
        } else {
            List<Object[]> rawResults = stockRepository.searchDrugsNational(query);
            List<SearchResultDTO.SearchItem> items = mapNationalResults(rawResults);
            items = rankAndSort(items);

            return buildNationalResponse(items, request.getPage(), request.getSize());
        }
    }

    private double resolveRadius(Integer radiusKm) {
        if (radiusKm == null) {
            return DEFAULT_RADIUS_KM;
        }
        return radiusKm;
    }

    private List<SearchResultDTO.SearchItem> mapGeoResults(List<Object[]> raw, double userLat, double userLon) {
        List<SearchResultDTO.SearchItem> items = new ArrayList<>();
        for (Object[] row : raw) {
            SearchResultDTO.SearchItem item = mapRow(row);
            if (row[10] != null) {
                double distanceMeters = ((Number) row[10]).doubleValue();
                item.setDistanceKm(distanceMeters / 1000.0);
            } else {
                // Calculate approximate distance if not provided by query
                if (item.getLatitude() != null && item.getLongitude() != null) {
                    item.setDistanceKm(haversineDistance(userLat, userLon, item.getLatitude(), item.getLongitude()));
                }
            }
            items.add(item);
        }
        return items;
    }

    private List<SearchResultDTO.SearchItem> mapNationalResults(List<Object[]> raw) {
        List<SearchResultDTO.SearchItem> items = new ArrayList<>();
        for (Object[] row : raw) {
            SearchResultDTO.SearchItem item = mapRow(row);
            item.setDistanceKm(null);
            items.add(item);
        }
        return items;
    }

    private SearchResultDTO.SearchItem mapRow(Object[] row) {
        SearchResultDTO.SearchItem item = new SearchResultDTO.SearchItem();

        // row: [0]=stockId, [1]=quantity, [2]=updatedAt, [3]=drugId, [4]=drugName,
        //       [5]=pharmacyId, [6]=pharmacyName, [7]=region, [8]=city,
        //       [9]=latitude, [10]=longitude (and potentially [11]=distanceMeters for geo)
        if (row[5] != null) item.setPharmacyId(toUUID(row[5]));
        if (row[6] != null) item.setPharmacyName((String) row[6]);
        if (row[7] != null) item.setRegion((String) row[7]);
        if (row[8] != null) item.setCity((String) row[8]);
        if (row[9] != null) item.setLatitude(((Number) row[9]).doubleValue());
        if (row.length > 10 && row[10] != null) item.setLongitude(((Number) row[10]).doubleValue());
        if (row[3] != null) item.setDrugId(toUUID(row[3]));
        if (row[4] != null) item.setDrugName((String) row[4]);
        if (row[1] != null) item.setQuantity(((Number) row[1]).intValue());

        // Store updatedAt for freshness calculation (index [2])
        if (row[2] != null) {
            item.setStockUpdatedAt(row[2].toString());
        }

        return item;
    }

    private java.util.UUID toUUID(Object value) {
        if (value instanceof java.util.UUID) return (java.util.UUID) value;
        if (value instanceof String) return java.util.UUID.fromString((String) value);
        if (value instanceof byte[]) {
            // PostgreSQL UUID might come as byte array in some setups
            return java.util.UUID.nameUUIDFromBytes((byte[]) value);
        }
        return null;
    }

    private List<SearchResultDTO.SearchItem> rankAndSort(List<SearchResultDTO.SearchItem> items) {
        Instant now = Instant.now();

        for (SearchResultDTO.SearchItem item : items) {
            double proximityScore = 1.0;
            if (item.getDistanceKm() != null && item.getDistanceKm() > 0) {
                proximityScore = 1.0 / (1.0 + item.getDistanceKm());
            }

            // Freshness: hours since the stock was last updated
            double hoursSinceUpdate = 0.0;
            if (item.getStockUpdatedAt() != null) {
                try {
                    Instant updatedAt = Instant.parse(item.getStockUpdatedAt());
                    hoursSinceUpdate = ChronoUnit.HOURS.between(updatedAt, now);
                    if (hoursSinceUpdate < 0) hoursSinceUpdate = 0;
                } catch (Exception e) {
                    hoursSinceUpdate = 0.0;
                }
            }

            // Score = proximity * quantity / (1 + hours since update)
            double score = proximityScore * item.getQuantity() / (1.0 + hoursSinceUpdate);
            item.setScore(score);
        }

        items.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        return items;
    }

    private SearchResultDTO buildGeoResponse(List<SearchResultDTO.SearchItem> allItems, int page, int size) {
        int totalResults = allItems.size();
        int totalPages = (int) Math.ceil((double) totalResults / size);
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, totalResults);

        SearchResultDTO response = new SearchResultDTO();
        response.setType("geo");
        response.setPage(page);
        response.setSize(size);
        response.setTotalResults(totalResults);
        response.setTotalPages(totalPages);

        if (fromIndex < totalResults) {
            response.setResults(allItems.subList(fromIndex, toIndex));
        } else {
            response.setResults(Collections.emptyList());
        }

        return response;
    }

    private SearchResultDTO buildNationalResponse(List<SearchResultDTO.SearchItem> allItems, int page, int size) {
        int totalResults = allItems.size();
        int totalPages = (int) Math.ceil((double) totalResults / size);

        // Group by region -> city
        Map<String, Map<String, List<SearchResultDTO.SearchItem>>> regionCityMap = new HashMap<>();
        for (SearchResultDTO.SearchItem item : allItems) {
            String region = item.getRegion() != null ? item.getRegion() : "Unknown";
            String city = item.getCity() != null ? item.getCity() : "Unknown";
            regionCityMap.computeIfAbsent(region, k -> new HashMap<>())
                            .computeIfAbsent(city, k -> new ArrayList<>())
                            .add(item);
        }

        // Flatten paginated items for the response
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, totalResults);

        List<SearchResultDTO.SearchItem> pageItems;
        if (fromIndex < totalResults) {
            pageItems = allItems.subList(fromIndex, toIndex);
        } else {
            pageItems = Collections.emptyList();
        }

        // Build groups only for items in this page
        List<SearchResultDTO.NationalGroup> groups = new ArrayList<>();
        Map<String, Map<String, List<SearchResultDTO.SearchItem>>> pageRegionCityMap = new HashMap<>();
        for (SearchResultDTO.SearchItem item : pageItems) {
            String region = item.getRegion() != null ? item.getRegion() : "Unknown";
            String city = item.getCity() != null ? item.getCity() : "Unknown";
            pageRegionCityMap.computeIfAbsent(region, k -> new HashMap<>())
                            .computeIfAbsent(city, k -> new ArrayList<>())
                            .add(item);
        }

        // Sort regions and cities alphabetically
        List<String> sortedRegions = new ArrayList<>(pageRegionCityMap.keySet());
        Collections.sort(sortedRegions);

        for (String region : sortedRegions) {
            SearchResultDTO.NationalGroup group = new SearchResultDTO.NationalGroup();
            group.setRegion(region);

            Map<String, List<SearchResultDTO.SearchItem>> cityMap = pageRegionCityMap.get(region);
            List<String> sortedCities = new ArrayList<>(cityMap.keySet());
            Collections.sort(sortedCities);

            List<SearchResultDTO.CityGroup> cityGroups = new ArrayList<>();
            for (String city : sortedCities) {
                SearchResultDTO.CityGroup cityGroup = new SearchResultDTO.CityGroup();
                cityGroup.setCity(city);
                cityGroup.setPharmacies(cityMap.get(city));
                cityGroups.add(cityGroup);
            }

            group.setCities(cityGroups);
            groups.add(group);
        }

        SearchResultDTO response = new SearchResultDTO();
        response.setType("national");
        response.setPage(page);
        response.setSize(size);
        response.setTotalResults(totalResults);
        response.setTotalPages(totalPages);
        response.setGroups(groups);

        return response;
    }

    private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }
}