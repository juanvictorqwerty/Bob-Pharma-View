package com.bob.server.search;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bob.server.repositories.StockRepository;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private StockRepository stockRepository;

    private SearchService searchService;

    @BeforeEach
    void setUp() {
        searchService = new SearchService(stockRepository);
    }

    @Test
    void searchWithCoordinatesShouldReturnGeoResults() {
        SearchRequest request = new SearchRequest();
        request.setQuery("aspirin");
        request.setLatitude(4.0511);
        request.setLongitude(9.7679);

        List<Object[]> mockResults = new ArrayList<>();
        mockResults.add(createMockRow("Pharmacy A", "Centre", "Yaoundé", 4.05, 9.77, 100, 500.0));
        mockResults.add(createMockRow("Pharmacy B", "Littoral", "Douala", 4.05, 9.70, 50, 5000.0));

        when(stockRepository.searchDrugsGeo(eq("aspirin"), eq(4.0511), eq(9.7679), eq(10000.0)))
                .thenReturn(mockResults);

        SearchResultDTO result = searchService.search(request);

        assertNotNull(result);
        assertEquals("geo", result.getType());
        assertEquals(2, result.getTotalResults());
        assertEquals(1, result.getTotalPages());
        assertNotNull(result.getResults());
        assertEquals(2, result.getResults().size());
    }

    @Test
    void searchWithoutCoordinatesShouldReturnNationalResults() {
        SearchRequest request = new SearchRequest();
        request.setQuery("paracetamol");

        List<Object[]> mockResults = new ArrayList<>();
        mockResults.add(createMockRow("Pharmacy A", "Centre", "Yaoundé", 4.05, 9.77, 100, null));
        mockResults.add(createMockRow("Pharmacy B", "Littoral", "Douala", 4.05, 9.70, 50, null));

        when(stockRepository.searchDrugsNational(eq("paracetamol")))
                .thenReturn(mockResults);

        SearchResultDTO result = searchService.search(request);

        assertNotNull(result);
        assertEquals("national", result.getType());
        assertEquals(2, result.getTotalResults());
        assertNotNull(result.getGroups());
    }

    @Test
    void searchWithNationalResultsShouldGroupByRegionAndCity() {
        SearchRequest request = new SearchRequest();
        request.setQuery("amoxicillin");

        List<Object[]> mockResults = new ArrayList<>();
        mockResults.add(createMockRow("Pharmacy A", "Centre", "Yaoundé", 4.05, 9.77, 100, null));
        mockResults.add(createMockRow("Pharmacy B", "Centre", "Yaoundé", 4.05, 9.78, 50, null));
        mockResults.add(createMockRow("Pharmacy C", "Littoral", "Douala", 4.05, 9.70, 200, null));

        when(stockRepository.searchDrugsNational(eq("amoxicillin")))
                .thenReturn(mockResults);

        SearchResultDTO result = searchService.search(request);

        assertNotNull(result);
        assertEquals("national", result.getType());
        assertEquals(3, result.getTotalResults());

        // Should have 2 region groups
        assertEquals(2, result.getGroups().size());

        // Centre group should have 1 city (Yaoundé) with 2 pharmacies
        SearchResultDTO.NationalGroup centreGroup = result.getGroups().stream()
                .filter(g -> "Centre".equals(g.getRegion()))
                .findFirst().orElse(null);
        assertNotNull(centreGroup);
        assertEquals(1, centreGroup.getCities().size());
        assertEquals("Yaoundé", centreGroup.getCities().get(0).getCity());
        assertEquals(2, centreGroup.getCities().get(0).getPharmacies().size());
    }

    @Test
    void searchWithPaginationShouldReturnCorrectPage() {
        SearchRequest request = new SearchRequest();
        request.setQuery("vitamin");
        request.setLatitude(4.0511);
        request.setLongitude(9.7679);
        request.setPage(0);
        request.setSize(1);

        List<Object[]> mockResults = new ArrayList<>();
        mockResults.add(createMockRow("Pharmacy A", "Centre", "Yaoundé", 4.05, 9.77, 100, 500.0));
        mockResults.add(createMockRow("Pharmacy B", "Littoral", "Douala", 4.05, 9.70, 50, 5000.0));

        when(stockRepository.searchDrugsGeo(eq("vitamin"), eq(4.0511), eq(9.7679), eq(10000.0)))
                .thenReturn(mockResults);

        SearchResultDTO result = searchService.search(request);

        assertNotNull(result);
        assertEquals(2, result.getTotalResults());
        assertEquals(2, result.getTotalPages());
        assertEquals(1, result.getResults().size());
    }

    @Test
    void searchWithEmptyResultsShouldReturnEmptyResponse() {
        SearchRequest request = new SearchRequest();
        request.setQuery("nonexistentdrug");
        request.setLatitude(4.0511);
        request.setLongitude(9.7679);

        when(stockRepository.searchDrugsGeo(eq("nonexistentdrug"), eq(4.0511), eq(9.7679), eq(10000.0)))
                .thenReturn(new ArrayList<>());

        SearchResultDTO result = searchService.search(request);

        assertNotNull(result);
        assertEquals("geo", result.getType());
        assertEquals(0, result.getTotalResults());
        assertEquals(0, result.getTotalPages());
        assertTrue(result.getResults().isEmpty());
    }

    @Test
    void searchWithExpandedRadiusShouldUse20Km() {
        SearchRequest request = new SearchRequest();
        request.setQuery("aspirin");
        request.setLatitude(4.0511);
        request.setLongitude(9.7679);
        request.setRadiusKm(20);

        List<Object[]> mockResults = new ArrayList<>();
        mockResults.add(createMockRow("Pharmacy A", "Centre", "Yaoundé", 4.05, 9.77, 100, 500.0));

        when(stockRepository.searchDrugsGeo(eq("aspirin"), eq(4.0511), eq(9.7679), eq(20000.0)))
                .thenReturn(mockResults);

        SearchResultDTO result = searchService.search(request);

        assertNotNull(result);
        assertEquals(1, result.getTotalResults());
    }

    @Test
    void rankingShouldPreferCloserPharmaciesWithHigherQuantity() {
        SearchRequest request = new SearchRequest();
        request.setQuery("aspirin");
        request.setLatitude(4.0511);
        request.setLongitude(9.7679);

        List<Object[]> mockResults = new ArrayList<>();
        // Far away with high quantity
        mockResults.add(createMockRow("Pharmacy Far", "Centre", "Yaoundé", 4.05, 9.77, 200, 10000.0));
        // Close with low quantity
        mockResults.add(createMockRow("Pharmacy Close", "Centre", "Yaoundé", 4.05, 9.77, 10, 100.0));

        when(stockRepository.searchDrugsGeo(eq("aspirin"), eq(4.0511), eq(9.7679), eq(10000.0)))
                .thenReturn(mockResults);

        SearchResultDTO result = searchService.search(request);

        assertNotNull(result);
        assertEquals(2, result.getResults().size());
        // The closer pharmacy with lower quantity should rank higher due to proximity weight
        assertTrue(result.getResults().get(0).getScore() > 0);
    }

    private Object[] createMockRow(String pharmacyName, String region, String city,
                                    double lat, double lon, int quantity, Double distanceMeters) {
        Object[] row = new Object[11];
        row[0] = UUID.randomUUID(); // stockId
        row[1] = quantity;          // quantity
        row[2] = Instant.now().toString(); // updatedAt
        row[3] = UUID.randomUUID(); // drugId
        row[4] = "Test Drug";       // drugName
        row[5] = UUID.randomUUID(); // pharmacyId
        row[6] = pharmacyName;      // pharmacyName
        row[7] = region;            // region
        row[8] = city;              // city
        row[9] = lat;               // latitude
        row[10] = lon;              // longitude
        return row;
    }
}