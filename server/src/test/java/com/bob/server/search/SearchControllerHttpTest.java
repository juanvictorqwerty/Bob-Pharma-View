package com.bob.server.search;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.bob.server.auth.token.JwtService;
import com.bob.server.auth.token.TokenService;
import com.bob.server.repositories.UsersRepository;

@WebMvcTest(SearchController.class)
@AutoConfigureMockMvc(addFilters = false)
class SearchControllerHttpTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SearchService searchService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private UsersRepository usersRepository;

    @Test
    void searchDrugsWithQueryShouldReturn200() throws Exception {
        SearchResultDTO mockResult = new SearchResultDTO();
        mockResult.setType("national");
        mockResult.setPage(0);
        mockResult.setSize(10);
        mockResult.setTotalResults(0);
        mockResult.setTotalPages(0);
        mockResult.setResults(new ArrayList<>());

        when(searchService.search(any())).thenReturn(mockResult);

        mockMvc.perform(get("/api/search/drugs")
                .param("query", "aspirin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("national"))
                .andExpect(jsonPath("$.totalResults").value(0));
    }

    @Test
    void searchDrugsWithCoordinatesShouldReturn200() throws Exception {
        SearchResultDTO mockResult = new SearchResultDTO();
        mockResult.setType("geo");
        mockResult.setPage(0);
        mockResult.setSize(10);
        mockResult.setTotalResults(0);
        mockResult.setTotalPages(0);
        mockResult.setResults(new ArrayList<>());

        when(searchService.search(any())).thenReturn(mockResult);

        mockMvc.perform(get("/api/search/drugs")
                .param("query", "aspirin")
                .param("latitude", "4.0511")
                .param("longitude", "9.7679"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("geo"));
    }

    @Test
    void searchDrugsWithRadiusShouldReturn200() throws Exception {
        SearchResultDTO mockResult = new SearchResultDTO();
        mockResult.setType("geo");
        mockResult.setPage(0);
        mockResult.setSize(10);
        mockResult.setTotalResults(0);
        mockResult.setTotalPages(0);
        mockResult.setResults(new ArrayList<>());

        when(searchService.search(any())).thenReturn(mockResult);

        mockMvc.perform(get("/api/search/drugs")
                .param("query", "aspirin")
                .param("latitude", "4.0511")
                .param("longitude", "9.7679")
                .param("radiusKm", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("geo"));
    }

    @Test
    void searchDrugsWithPaginationShouldReturn200() throws Exception {
        SearchResultDTO mockResult = new SearchResultDTO();
        mockResult.setType("national");
        mockResult.setPage(1);
        mockResult.setSize(5);
        mockResult.setTotalResults(12);
        mockResult.setTotalPages(3);
        mockResult.setResults(new ArrayList<>());

        when(searchService.search(any())).thenReturn(mockResult);

        mockMvc.perform(get("/api/search/drugs")
                .param("query", "aspirin")
                .param("page", "1")
                .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalResults").value(12))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    @Test
    void searchDrugsWithoutQueryShouldReturn400() throws Exception {
        mockMvc.perform(get("/api/search/drugs"))
                .andExpect(status().isBadRequest());
    }
}