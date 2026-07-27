package com.bob.server.drug_update;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bob.server.model.Users;
import com.bob.server.repositories.PharmacyStaffRepository;
import com.bob.server.repositories.UsersRepository;

@RestController
@RequestMapping("/api/drugs")
public class UpdateDrugController {

    private final UpdateDrugService updateDrugService;
    private final UsersRepository usersRepository;
    private final PharmacyStaffRepository pharmacyStaffRepository;

    public UpdateDrugController(UpdateDrugService updateDrugService,
                                UsersRepository usersRepository,
                                PharmacyStaffRepository pharmacyStaffRepository) {
        this.updateDrugService = updateDrugService;
        this.usersRepository = usersRepository;
        this.pharmacyStaffRepository = pharmacyStaffRepository;
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateDrugs(@RequestParam("file") MultipartFile file,
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

        boolean isMember = pharmacyStaffRepository.existsByUserIdAndPharmacyId(user.getID(), pharmacyId);
        if (!isMember) {
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