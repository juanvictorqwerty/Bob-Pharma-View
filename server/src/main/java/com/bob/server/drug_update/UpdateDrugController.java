package com.bob.server.drug_update;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bob.server.model.Pharmacy;
import com.bob.server.model.Users;
import com.bob.server.repositories.PharmacyRepository;
import com.bob.server.repositories.PharmacyStaffRepository;
import com.bob.server.repositories.UsersRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;

@RestController
@RequestMapping("/api/drugs")
public class UpdateDrugController {

    private final UpdateDrugService updateDrugService;
    private final UsersRepository usersRepository;
    private final PharmacyStaffRepository pharmacyStaffRepository;
    private final PharmacyRepository pharmacyRepository;

    public UpdateDrugController(UpdateDrugService updateDrugService,
                                UsersRepository usersRepository,
                                PharmacyStaffRepository pharmacyStaffRepository,
                                PharmacyRepository pharmacyRepository) {
        this.updateDrugService = updateDrugService;
        this.usersRepository = usersRepository;
        this.pharmacyStaffRepository = pharmacyStaffRepository;
        this.pharmacyRepository = pharmacyRepository;
    }

    @PostMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update drugs from Excel file",
               description = "Upload an Excel file (.xlsx or .xls) to update drug stock for a pharmacy")
    public ResponseEntity<?> updateDrugs(
            @Parameter(description = "Excel file (.xlsx or .xls)", required = true,
                       content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Pharmacy ID", required = true)
            @RequestParam("pharmacyId") UUID pharmacyId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String userEmail = authentication.getName();
        Users user = usersRepository.findByEmail(userEmail);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(UpdateDrugValidation.ACCESS_DENIED.getMessage());
        }

        // Check if user is the pharmacy creator
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId).orElse(null);
        boolean isCreator = pharmacy != null && pharmacy.getCreatorId().getID().equals(user.getID());

        // Check if user is a non-suspended staff member
        boolean isActiveStaff = false;
        if (!isCreator) {
            var staffRecords = pharmacyStaffRepository.findByUserIdAndPharmacyId(user.getID(), pharmacyId);
            isActiveStaff = staffRecords.stream().anyMatch(s -> !s.isSuspended());
        }

        if (!isCreator && !isActiveStaff) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(UpdateDrugValidation.ACCESS_DENIED.getMessage());
        }

        try {
            int rowsProcessed = updateDrugService.updateDrugsFromExcel(file, pharmacyId);
            return ResponseEntity.ok("Rows processed: " + rowsProcessed);
        } catch (UpdateDrugException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }
}