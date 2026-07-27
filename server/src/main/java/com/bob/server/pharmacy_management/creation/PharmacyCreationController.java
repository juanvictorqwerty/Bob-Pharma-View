package com.bob.server.pharmacy_management.creation;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bob.server.model.Users;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/pharmacies")
public class PharmacyCreationController {

    private final PharmacyCreationService pharmacyCreationService;

    public PharmacyCreationController(PharmacyCreationService pharmacyCreationService) {
        this.pharmacyCreationService = pharmacyCreationService;
    }

    @PostMapping("/Create")
    public ResponseEntity<?> createPharmacy(
            @Valid @RequestBody PharmacyCreationDTO dto) {
        Users currentUser = resolveCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            PharmacyResponseDTO response = pharmacyCreationService.createPharmacy(dto, currentUser);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{pharmacyId}/Approve")
    public ResponseEntity<?> approvePharmacy(
            @PathVariable UUID pharmacyId) {
        try {
            PharmacyResponseDTO response = pharmacyCreationService.approvePharmacy(pharmacyId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping({"/{pharmacyId}/Staff", "/{pharmacyId}/staff"})
    public ResponseEntity<?> addStaff(
            @PathVariable UUID pharmacyId,
            @Valid @RequestBody PharmacyStaffAssignmentDTO dto) {
        Users currentUser = resolveCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            PharmacyStaffResponseDTO response = pharmacyCreationService.addStaff(pharmacyId, dto, currentUser);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{pharmacyId}/Staff/{staffId}")
    public ResponseEntity<?> removeStaff(
            @PathVariable UUID pharmacyId,
            @PathVariable UUID staffId) {
        Users currentUser = resolveCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            pharmacyCreationService.removeStaff(pharmacyId, staffId, currentUser);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{pharmacyId}/Suspend")
    public ResponseEntity<?> suspendPharmacy(
            @PathVariable UUID pharmacyId) {
        try {
            PharmacyResponseDTO response = pharmacyCreationService.suspendPharmacy(pharmacyId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{pharmacyId}/Unsuspend")
    public ResponseEntity<?> unsuspendPharmacy(
            @PathVariable UUID pharmacyId) {
        try {
            PharmacyResponseDTO response = pharmacyCreationService.unsuspendPharmacy(pharmacyId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/MyPharmacies")
    public ResponseEntity<List<PharmacyResponseDTO>> getMyPharmacies() {
        Users currentUser = resolveCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<PharmacyResponseDTO> response = pharmacyCreationService.getMyPharmacies(currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/Nearby")
    public ResponseEntity<List<PharmacyResponseDTO>> findNearbyPharmacies(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "5000") double distance) {
        List<PharmacyResponseDTO> response = pharmacyCreationService.findNearbyPharmacies(latitude, longitude, distance);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{pharmacyId}/MyStaff")
    public ResponseEntity<?> getPharmacyStaffForMembers(
            @PathVariable UUID pharmacyId) {
        Users currentUser = resolveCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            List<PharmacyStaffResponseDTO> response = pharmacyCreationService.getPharmacyStaffForMembers(pharmacyId, currentUser);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{pharmacyId}")
    public ResponseEntity<?> getPharmacy(
            @PathVariable UUID pharmacyId) {
        try {
            PharmacyResponseDTO response = pharmacyCreationService.getPharmacyById(pharmacyId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<Page<PharmacyResponseDTO>> getPharmacies(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<PharmacyResponseDTO> response = pharmacyCreationService.searchPharmacies(name, region, city, page, size);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{pharmacyId}/Update")
    public ResponseEntity<?> updatePharmacy(
            @PathVariable UUID pharmacyId,
            @Valid @RequestBody PharmacyCreationDTO dto) {
        Users currentUser = resolveCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            PharmacyResponseDTO response = pharmacyCreationService.updatePharmacy(pharmacyId, dto, currentUser);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{pharmacyId}/Staff/{staffId}/Suspend")
    public ResponseEntity<?> suspendStaff(
            @PathVariable UUID pharmacyId,
            @PathVariable UUID staffId) {
        Users currentUser = resolveCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            PharmacyStaffResponseDTO response = pharmacyCreationService.suspendStaff(pharmacyId, staffId, currentUser);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{pharmacyId}/Staff/{staffId}/Unsuspend")
    public ResponseEntity<?> unsuspendStaff(
            @PathVariable UUID pharmacyId,
            @PathVariable UUID staffId) {
        Users currentUser = resolveCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            PharmacyStaffResponseDTO response = pharmacyCreationService.unsuspendStaff(pharmacyId, staffId, currentUser);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/Staff/ByUser/{userId}")
    public ResponseEntity<List<PharmacyStaffResponseDTO>> getStaffByUser(
            @PathVariable UUID userId) {
        List<PharmacyStaffResponseDTO> response = pharmacyCreationService.getStaffByUser(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{pharmacyId}/Staff/{staffId}/ChangeRole")
    public ResponseEntity<?> changeStaffRole(
            @PathVariable UUID pharmacyId,
            @PathVariable UUID staffId,
            @Valid @RequestBody PharmacyStaffRoleChangeDTO dto) {
        Users currentUser = resolveCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            PharmacyStaffResponseDTO response = pharmacyCreationService.changeStaffRole(pharmacyId, staffId, dto.getNewRole(), currentUser);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{pharmacyId}/TransferOwnership")
    public ResponseEntity<?> transferOwnership(
            @PathVariable UUID pharmacyId,
            @Valid @RequestBody PharmacyTransferDTO dto) {
        Users currentUser = resolveCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            PharmacyResponseDTO response = pharmacyCreationService.transferOwnership(pharmacyId, dto.getNewOwnerEmail(), currentUser);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{pharmacyId}/Deactivate")
    public ResponseEntity<?> deactivatePharmacy(
            @PathVariable UUID pharmacyId) {
        Users currentUser = resolveCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            PharmacyResponseDTO response = pharmacyCreationService.deactivatePharmacy(pharmacyId, currentUser);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{pharmacyId}/Reactivate")
    public ResponseEntity<?> reactivatePharmacy(
            @PathVariable UUID pharmacyId) {
        Users currentUser = resolveCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            PharmacyResponseDTO response = pharmacyCreationService.reactivatePharmacy(pharmacyId, currentUser);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{pharmacyId}/Staff/Me")
    public ResponseEntity<?> removeSelfFromStaff(
            @PathVariable UUID pharmacyId) {
        Users currentUser = resolveCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            pharmacyCreationService.removeSelfFromStaff(pharmacyId, currentUser);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{pharmacyId}/Staff/Count")
    public ResponseEntity<Long> getPharmacyStaffCount(
            @PathVariable UUID pharmacyId) {
        long count = pharmacyCreationService.getPharmacyStaffCount(pharmacyId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/{pharmacyId}/Staff")
    public ResponseEntity<List<PharmacyStaffResponseDTO>> getPharmacyStaff(
            @PathVariable UUID pharmacyId) {
        List<PharmacyStaffResponseDTO> response = pharmacyCreationService.getPharmacyStaff(pharmacyId);
        return ResponseEntity.ok(response);
    }

    private Users resolveCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof Users users) {
            return users;
        }

        Users user = new Users();
        user.setEmail(authentication.getName());
        return user;
    }
}
