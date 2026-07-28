package com.bob.server.drug_update;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.bob.server.auth.token.JwtService;
import com.bob.server.auth.token.TokenService;
import com.bob.server.model.Pharmacy;
import com.bob.server.model.Users;
import com.bob.server.repositories.PharmacyRepository;
import com.bob.server.repositories.PharmacyStaffRepository;
import com.bob.server.repositories.UsersRepository;

@WebMvcTest(UpdateDrugController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(username = "staff@example.com")
class UpdateDrugControllerHttpTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UpdateDrugService updateDrugService;

    @MockitoBean
    private UsersRepository usersRepository;

    @MockitoBean
    private PharmacyStaffRepository pharmacyStaffRepository;

    @MockitoBean
    private PharmacyRepository pharmacyRepository;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private TokenService tokenService;

    private final UUID pharmacyId = UUID.randomUUID();
    private final String userEmail = "staff@example.com";

    private Users createUser() {
        Users user = new Users();
        user.setID(UUID.randomUUID());
        user.setEmail(userEmail);
        return user;
    }

    private Pharmacy createPharmacy(Users creator) {
        Pharmacy pharmacy = new Pharmacy();
        pharmacy.setID(pharmacyId);
        pharmacy.setCreatorId(creator);
        return pharmacy;
    }

    @Test
    void updateDrugsWithValidFileShouldReturn200() throws Exception {
        Users user = createUser();
        Pharmacy pharmacy = createPharmacy(user);
        when(usersRepository.findByEmail(userEmail)).thenReturn(user);
        when(pharmacyRepository.findById(pharmacyId)).thenReturn(java.util.Optional.of(pharmacy));
        when(updateDrugService.updateDrugsFromExcel(any(), eq(pharmacyId))).thenReturn(5);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "drugs.xlsx",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "fake-excel-content".getBytes()
        );

        mockMvc.perform(multipart("/api/drugs/update")
                .file(file)
                .param("pharmacyId", pharmacyId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("Success"));
    }

    @Test
    void updateDrugsWhenUserNotStaffShouldReturn403() throws Exception {
        Users user = createUser();
        Users otherUser = new Users();
        otherUser.setID(UUID.randomUUID());
        Pharmacy pharmacy = createPharmacy(otherUser);
        when(usersRepository.findByEmail(userEmail)).thenReturn(user);
        when(pharmacyRepository.findById(pharmacyId)).thenReturn(java.util.Optional.of(pharmacy));
        when(pharmacyStaffRepository.findByUserIdAndPharmacyId(user.getID(), pharmacyId)).thenReturn(java.util.Collections.emptyList());

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "drugs.xlsx",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "fake-excel-content".getBytes()
        );

        mockMvc.perform(multipart("/api/drugs/update")
                .file(file)
                .param("pharmacyId", pharmacyId.toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateDrugsWithoutAuthShouldReturn401() throws Exception {
        SecurityContextHolder.clearContext();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "drugs.xlsx",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "fake-excel-content".getBytes()
        );

        mockMvc.perform(multipart("/api/drugs/update")
                .file(file)
                .param("pharmacyId", pharmacyId.toString()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateDrugsWithServiceExceptionShouldReturn400() throws Exception {
        Users user = createUser();
        Pharmacy pharmacy = createPharmacy(user);
        when(usersRepository.findByEmail(userEmail)).thenReturn(user);
        when(pharmacyRepository.findById(pharmacyId)).thenReturn(java.util.Optional.of(pharmacy));
        when(updateDrugService.updateDrugsFromExcel(any(), eq(pharmacyId)))
                .thenThrow(new UpdateDrugException(UpdateDrugValidation.FILE_EMPTY));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "drugs.xlsx",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "fake-excel-content".getBytes()
        );

        mockMvc.perform(multipart("/api/drugs/update")
                .file(file)
                .param("pharmacyId", pharmacyId.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(UpdateDrugValidation.FILE_EMPTY.getMessage()));
    }

    @Test
    void updateDrugsWhenUserNotFoundShouldReturn403() throws Exception {
        when(usersRepository.findByEmail("nonexistent@example.com")).thenReturn(null);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "drugs.xlsx",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "fake-excel-content".getBytes()
        );

        mockMvc.perform(multipart("/api/drugs/update")
                .file(file)
                .param("pharmacyId", pharmacyId.toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateDrugsWithMissingPharmacyIdShouldReturn400() throws Exception {
        Users user = createUser();
        Pharmacy pharmacy = createPharmacy(user);
        when(usersRepository.findByEmail(userEmail)).thenReturn(user);
        when(pharmacyRepository.findById(pharmacyId)).thenReturn(java.util.Optional.of(pharmacy));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "drugs.xlsx",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "fake-excel-content".getBytes()
        );

        mockMvc.perform(multipart("/api/drugs/update")
                .file(file))
                .andExpect(status().isBadRequest());
    }
}
