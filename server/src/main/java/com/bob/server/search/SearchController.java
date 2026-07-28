package com.bob.server.search;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/drugs")
    public ResponseEntity<?> searchDrugs(
            @RequestParam("query") String query,
            @RequestParam(value = "latitude", required = false) Double latitude,
            @RequestParam(value = "longitude", required = false) Double longitude,
            @RequestParam(value = "radiusKm", required = false) Integer radiusKm,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        SearchRequest request = new SearchRequest();
        request.setQuery(query);
        request.setLatitude(latitude);
        request.setLongitude(longitude);
        request.setRadiusKm(radiusKm);
        request.setPage(page);
        request.setSize(size);

        try {
            SearchResultDTO result = searchService.search(request);
            return ResponseEntity.ok(result);
        } catch (SearchException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}