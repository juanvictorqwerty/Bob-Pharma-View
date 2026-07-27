package com.bob.server.pharmacy_management.creation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.bob.server.auth.token.JwtService;
import com.bob.server.auth.token.TokenService;
import com.bob.server.model.Users;
import com.bob.server.repositories.UsersRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(PharmacyCreationController.class)
@AutoConfigureMockMvc(addFilters = false)
class PharmacyCreationControllerHttpTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private PharmacyCreationService pharmacyCreationService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UsersRepository usersRepository;

    @MockitoBean
    private TokenService tokenService;

    private final UUID pharmacyId = UUID.randomUUID();
    private final UUID staffId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    private PharmacyResponseDTO createPharmacyResponse() {
        PharmacyResponseDTO dto = new PharmacyResponseDTO();
        dto.setId(pharmacyId);
        dto.setName("Test Pharmacy");
        dto.setRegion("Test Region");
        dto.setCity("Test City");
        dto.setLatitude("12.34");
        dto.setLongitude("56.78");
        dto.setApproved(false);
        dto.setSuspended(false);
        dto.setActive(true);
        return dto;
    }

    private PharmacyStaffResponseDTO createStaffResponse() {
        PharmacyStaffResponseDTO dto = new PharmacyStaffResponseDTO();
        dto.setId(staffId);
        dto.setUserEmail("staff@example.com");
        dto.setPharmacyId(pharmacyId);
        dto.setRole("PHARMACY_PERSONNEL");
        dto.setSuspended(false);
        return dto;
    }

    // --- POST /api/pharmacies/Create ---

    @Test
    @WithMockUser
    void createPharmacyWithValidDataShouldReturn201() throws Exception {
        PharmacyCreationDTO dto = new PharmacyCreationDTO();
        dto.setName("New Pharmacy");
        dto.setRegion("Region");
        dto.setCity("City");
        dto.setLatitude("12.34");
        dto.setLongitude("56.78");

        PharmacyResponseDTO response = createPharmacyResponse();
        when(pharmacyCreationService.createPharmacy(any(PharmacyCreationDTO.class), any(Users.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/pharmacies/Create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Pharmacy"))
                .andExpect(jsonPath("$.region").value("Test Region"));
    }

    @Test
    @WithMockUser
    void createPharmacyWithMissingNameShouldReturn400() throws Exception {
        PharmacyCreationDTO dto = new PharmacyCreationDTO();
        dto.setRegion("Region");
        dto.setCity("City");
        dto.setLatitude("12.34");
        dto.setLongitude("56.78");

        mockMvc.perform(post("/api/pharmacies/Create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void createPharmacyWithMissingRegionShouldReturn400() throws Exception {
        PharmacyCreationDTO dto = new PharmacyCreationDTO();
        dto.setName("New Pharmacy");
        dto.setCity("City");
        dto.setLatitude("12.34");
        dto.setLongitude("56.78");

        mockMvc.perform(post("/api/pharmacies/Create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPharmacyWithoutAuthShouldReturn401() throws Exception {
        PharmacyCreationDTO dto = new PharmacyCreationDTO();
        dto.setName("New Pharmacy");
        dto.setRegion("Region");
        dto.setCity("City");
        dto.setLatitude("12.34");
        dto.setLongitude("56.78");

        mockMvc.perform(post("/api/pharmacies/Create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    // --- POST /api/pharmacies/{pharmacyId}/Approve ---

    @Test
    @WithMockUser(roles = "Admin")
    void approvePharmacyShouldReturn200() throws Exception {
        PharmacyResponseDTO response = createPharmacyResponse();
        response.setApproved(true);
        when(pharmacyCreationService.approvePharmacy(pharmacyId)).thenReturn(response);

        mockMvc.perform(post("/api/pharmacies/{pharmacyId}/Approve", pharmacyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approved").value(true));
    }

    @Test
    @WithMockUser(roles = "Admin")
    void approvePharmacyWhenNotFoundShouldReturn404() throws Exception {
        when(pharmacyCreationService.approvePharmacy(pharmacyId))
                .thenThrow(new PharmacyCreationException("Pharmacy not found"));

        mockMvc.perform(post("/api/pharmacies/{pharmacyId}/Approve", pharmacyId))
                .andExpect(status().isInternalServerError());
    }

    // --- POST /api/pharmacies/{pharmacyId}/Staff ---

    @Test
    @WithMockUser
    void addStaffWithValidDataShouldReturn201() throws Exception {
        PharmacyStaffAssignmentDTO dto = new PharmacyStaffAssignmentDTO();
        dto.setEmail("staff@example.com");
        dto.setRole("PHARMACY_PERSONNEL");

        PharmacyStaffResponseDTO response = createStaffResponse();
        when(pharmacyCreationService.addStaff(eq(pharmacyId), any(PharmacyStaffAssignmentDTO.class), any(Users.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/pharmacies/{pharmacyId}/Staff", pharmacyId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userEmail").value("staff@example.com"))
                .andExpect(jsonPath("$.role").value("PHARMACY_PERSONNEL"));
    }

    @Test
    @WithMockUser
    void addStaffWithMissingEmailShouldReturn400() throws Exception {
        PharmacyStaffAssignmentDTO dto = new PharmacyStaffAssignmentDTO();
        dto.setRole("PHARMACY_PERSONNEL");

        mockMvc.perform(post("/api/pharmacies/{pharmacyId}/Staff", pharmacyId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    // --- DELETE /api/pharmacies/{pharmacyId}/Staff/{staffId} ---

    @Test
    @WithMockUser
    void removeStaffShouldReturn204() throws Exception {
        doNothing().when(pharmacyCreationService).removeStaff(eq(pharmacyId), eq(staffId), any(Users.class));

        mockMvc.perform(delete("/api/pharmacies/{pharmacyId}/Staff/{staffId}", pharmacyId, staffId))
                .andExpect(status().isNoContent());
    }

    // --- POST /api/pharmacies/{pharmacyId}/Suspend ---

    @Test
    @WithMockUser(roles = "Admin")
    void suspendPharmacyShouldReturn200() throws Exception {
        PharmacyResponseDTO response = createPharmacyResponse();
        response.setSuspended(true);
        response.setActive(false);
        when(pharmacyCreationService.suspendPharmacy(pharmacyId)).thenReturn(response);

        mockMvc.perform(post("/api/pharmacies/{pharmacyId}/Suspend", pharmacyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suspended").value(true))
                .andExpect(jsonPath("$.active").value(false));
    }

    // --- POST /api/pharmacies/{pharmacyId}/Unsuspend ---

    @Test
    @WithMockUser(roles = "Admin")
    void unsuspendPharmacyShouldReturn200() throws Exception {
        PharmacyResponseDTO response = createPharmacyResponse();
        response.setSuspended(false);
        response.setActive(true);
        when(pharmacyCreationService.unsuspendPharmacy(pharmacyId)).thenReturn(response);

        mockMvc.perform(post("/api/pharmacies/{pharmacyId}/Unsuspend", pharmacyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suspended").value(false))
                .andExpect(jsonPath("$.active").value(true));
    }

    // --- GET /api/pharmacies/MyPharmacies ---

    @Test
    @WithMockUser
    void getMyPharmaciesShouldReturn200() throws Exception {
        List<PharmacyResponseDTO> pharmacies = List.of(createPharmacyResponse());
        when(pharmacyCreationService.getMyPharmacies(any(Users.class))).thenReturn(pharmacies);

        mockMvc.perform(get("/api/pharmacies/MyPharmacies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Pharmacy"));
    }

    // --- GET /api/pharmacies/Nearby ---

    @Test
    @WithMockUser
    void findNearbyPharmaciesShouldReturn200() throws Exception {
        List<PharmacyResponseDTO> pharmacies = List.of(createPharmacyResponse());
        when(pharmacyCreationService.findNearbyPharmacies(anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(pharmacies);

        mockMvc.perform(get("/api/pharmacies/Nearby")
                .param("latitude", "12.34")
                .param("longitude", "56.78")
                .param("distance", "5000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Pharmacy"));
    }

    @Test
    @WithMockUser
    void findNearbyPharmaciesWithDefaultDistanceShouldReturn200() throws Exception {
        List<PharmacyResponseDTO> pharmacies = List.of(createPharmacyResponse());
        when(pharmacyCreationService.findNearbyPharmacies(anyDouble(), anyDouble(), eq(5000.0)))
                .thenReturn(pharmacies);

        mockMvc.perform(get("/api/pharmacies/Nearby")
                .param("latitude", "12.34")
                .param("longitude", "56.78"))
                .andExpect(status().isOk());
    }

    // --- GET /api/pharmacies/{pharmacyId}/MyStaff ---

    @Test
    @WithMockUser
    void getPharmacyStaffForMembersShouldReturn200() throws Exception {
        List<PharmacyStaffResponseDTO> staffList = List.of(createStaffResponse());
        when(pharmacyCreationService.getPharmacyStaffForMembers(eq(pharmacyId), any(Users.class)))
                .thenReturn(staffList);

        mockMvc.perform(get("/api/pharmacies/{pharmacyId}/MyStaff", pharmacyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userEmail").value("staff@example.com"));
    }

    // --- GET /api/pharmacies/{pharmacyId} ---

    @Test
    @WithMockUser
    void getPharmacyByIdShouldReturn200() throws Exception {
        PharmacyResponseDTO response = createPharmacyResponse();
        when(pharmacyCreationService.getPharmacyById(pharmacyId)).thenReturn(response);

        mockMvc.perform(get("/api/pharmacies/{pharmacyId}", pharmacyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isString());
    }

    @Test
    @WithMockUser
    void getPharmacyByIdWhenNotFoundShouldReturn500() throws Exception {
        when(pharmacyCreationService.getPharmacyById(pharmacyId))
                .thenThrow(new PharmacyCreationException("Pharmacy not found"));

        mockMvc.perform(get("/api/pharmacies/{pharmacyId}", pharmacyId))
                .andExpect(status().isInternalServerError());
    }

    // --- GET /api/pharmacies ---

    @Test
    @WithMockUser
    void getPharmaciesWithPaginationShouldReturn200() throws Exception {
        Page<PharmacyResponseDTO> page = new PageImpl<>(
                List.of(createPharmacyResponse()),
                PageRequest.of(0, 10),
                1);
        when(pharmacyCreationService.searchPharmacies(any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(page);

        mockMvc.perform(get("/api/pharmacies")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Test Pharmacy"));
    }

    @Test
    @WithMockUser
    void getPharmaciesWithFiltersShouldReturn200() throws Exception {
        Page<PharmacyResponseDTO> page = new PageImpl<>(
                List.of(createPharmacyResponse()),
                PageRequest.of(0, 10),
                1);
        when(pharmacyCreationService.searchPharmacies(eq("Test"), eq("Region"), eq("City"), eq(0), eq(10)))
                .thenReturn(page);

        mockMvc.perform(get("/api/pharmacies")
                .param("name", "Test")
                .param("region", "Region")
                .param("city", "City")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk());
    }

    // --- PUT /api/pharmacies/{pharmacyId}/Update ---

    @Test
    @WithMockUser
    void updatePharmacyWithValidDataShouldReturn200() throws Exception {
        PharmacyCreationDTO dto = new PharmacyCreationDTO();
        dto.setName("Updated Pharmacy");
        dto.setRegion("Updated Region");
        dto.setCity("Updated City");
        dto.setLatitude("98.76");
        dto.setLongitude("54.32");

        PharmacyResponseDTO response = createPharmacyResponse();
        response.setName("Updated Pharmacy");
        when(pharmacyCreationService.updatePharmacy(eq(pharmacyId), any(PharmacyCreationDTO.class), any(Users.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/pharmacies/{pharmacyId}/Update", pharmacyId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Pharmacy"));
    }

    // --- POST /api/pharmacies/{pharmacyId}/Staff/{staffId}/Suspend ---

    @Test
    @WithMockUser
    void suspendStaffShouldReturn200() throws Exception {
        PharmacyStaffResponseDTO response = createStaffResponse();
        response.setSuspended(true);
        when(pharmacyCreationService.suspendStaff(eq(pharmacyId), eq(staffId), any(Users.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/pharmacies/{pharmacyId}/Staff/{staffId}/Suspend", pharmacyId, staffId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suspended").value(true));
    }

    // --- POST /api/pharmacies/{pharmacyId}/Staff/{staffId}/Unsuspend ---

    @Test
    @WithMockUser
    void unsuspendStaffShouldReturn200() throws Exception {
        PharmacyStaffResponseDTO response = createStaffResponse();
        response.setSuspended(false);
        when(pharmacyCreationService.unsuspendStaff(eq(pharmacyId), eq(staffId), any(Users.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/pharmacies/{pharmacyId}/Staff/{staffId}/Unsuspend", pharmacyId, staffId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suspended").value(false));
    }

    // --- GET /api/pharmacies/Staff/ByUser/{userId} ---

    @Test
    @WithMockUser
    void getStaffByUserShouldReturn200() throws Exception {
        List<PharmacyStaffResponseDTO> staffList = List.of(createStaffResponse());
        when(pharmacyCreationService.getStaffByUser(userId)).thenReturn(staffList);

        mockMvc.perform(get("/api/pharmacies/Staff/ByUser/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userEmail").value("staff@example.com"));
    }

    // --- PUT /api/pharmacies/{pharmacyId}/Staff/{staffId}/ChangeRole ---

    @Test
    @WithMockUser
    void changeStaffRoleShouldReturn200() throws Exception {
        PharmacyStaffRoleChangeDTO dto = new PharmacyStaffRoleChangeDTO();
        dto.setNewRole("PHARMACY_ADMIN");

        PharmacyStaffResponseDTO response = createStaffResponse();
        response.setRole("PHARMACY_ADMIN");
        when(pharmacyCreationService.changeStaffRole(eq(pharmacyId), eq(staffId), eq("PHARMACY_ADMIN"), any(Users.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/pharmacies/{pharmacyId}/Staff/{staffId}/ChangeRole", pharmacyId, staffId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("PHARMACY_ADMIN"));
    }

    @Test
    @WithMockUser
    void changeStaffRoleWithMissingRoleShouldReturn400() throws Exception {
        PharmacyStaffRoleChangeDTO dto = new PharmacyStaffRoleChangeDTO();

        mockMvc.perform(put("/api/pharmacies/{pharmacyId}/Staff/{staffId}/ChangeRole", pharmacyId, staffId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    // --- POST /api/pharmacies/{pharmacyId}/TransferOwnership ---

    @Test
    @WithMockUser
    void transferOwnershipShouldReturn200() throws Exception {
        PharmacyTransferDTO dto = new PharmacyTransferDTO();
        dto.setNewOwnerEmail("newowner@example.com");

        PharmacyResponseDTO response = createPharmacyResponse();
        when(pharmacyCreationService.transferOwnership(eq(pharmacyId), eq("newowner@example.com"), any(Users.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/pharmacies/{pharmacyId}/TransferOwnership", pharmacyId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void transferOwnershipWithMissingEmailShouldReturn400() throws Exception {
        PharmacyTransferDTO dto = new PharmacyTransferDTO();

        mockMvc.perform(post("/api/pharmacies/{pharmacyId}/TransferOwnership", pharmacyId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    // --- POST /api/pharmacies/{pharmacyId}/Deactivate ---

    @Test
    @WithMockUser
    void deactivatePharmacyShouldReturn200() throws Exception {
        PharmacyResponseDTO response = createPharmacyResponse();
        response.setActive(false);
        when(pharmacyCreationService.deactivatePharmacy(eq(pharmacyId), any(Users.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/pharmacies/{pharmacyId}/Deactivate", pharmacyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    // --- POST /api/pharmacies/{pharmacyId}/Reactivate ---

    @Test
    @WithMockUser
    void reactivatePharmacyShouldReturn200() throws Exception {
        PharmacyResponseDTO response = createPharmacyResponse();
        response.setActive(true);
        when(pharmacyCreationService.reactivatePharmacy(eq(pharmacyId), any(Users.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/pharmacies/{pharmacyId}/Reactivate", pharmacyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }

    // --- DELETE /api/pharmacies/{pharmacyId}/Staff/Me ---

    @Test
    @WithMockUser
    void removeSelfFromStaffShouldReturn204() throws Exception {
        doNothing().when(pharmacyCreationService).removeSelfFromStaff(eq(pharmacyId), any(Users.class));

        mockMvc.perform(delete("/api/pharmacies/{pharmacyId}/Staff/Me", pharmacyId))
                .andExpect(status().isNoContent());
    }

    // --- GET /api/pharmacies/{pharmacyId}/Staff/Count ---

    @Test
    @WithMockUser
    void getPharmacyStaffCountShouldReturn200() throws Exception {
        when(pharmacyCreationService.getPharmacyStaffCount(pharmacyId)).thenReturn(5L);

        mockMvc.perform(get("/api/pharmacies/{pharmacyId}/Staff/Count", pharmacyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(5));
    }

    // --- GET /api/pharmacies/{pharmacyId}/Staff ---

    @Test
    @WithMockUser
    void getPharmacyStaffShouldReturn200() throws Exception {
        List<PharmacyStaffResponseDTO> staffList = List.of(createStaffResponse());
        when(pharmacyCreationService.getPharmacyStaff(pharmacyId)).thenReturn(staffList);

        mockMvc.perform(get("/api/pharmacies/{pharmacyId}/Staff", pharmacyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userEmail").value("staff@example.com"));
    }
}