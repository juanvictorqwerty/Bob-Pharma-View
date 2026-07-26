package com.bob.server.pharmacy_management.creation;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ResponseEntity<PharmacyResponseDTO> createPharmacy(
            @Valid @RequestBody PharmacyCreationDTO dto,
            @AuthenticationPrincipal Users currentUser) {
        PharmacyResponseDTO response = pharmacyCreationService.createPharmacy(dto, currentUser);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/{pharmacyId}/Approve")
    public ResponseEntity<PharmacyResponseDTO> approvePharmacy(
            @PathVariable UUID pharmacyId) {
        PharmacyResponseDTO response = pharmacyCreationService.approvePharmacy(pharmacyId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{pharmacyId}/Staff")
    public ResponseEntity<PharmacyStaffResponseDTO> addStaff(
            @PathVariable UUID pharmacyId,
            @Valid @RequestBody PharmacyStaffAssignmentDTO dto,
            @AuthenticationPrincipal Users currentUser) {
        PharmacyStaffResponseDTO response = pharmacyCreationService.addStaff(pharmacyId, dto, currentUser);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @DeleteMapping("/{pharmacyId}/Staff/{staffId}")
    public ResponseEntity<Void> removeStaff(
            @PathVariable UUID pharmacyId,
            @PathVariable UUID staffId,
            @AuthenticationPrincipal Users currentUser) {
        pharmacyCreationService.removeStaff(pharmacyId, staffId, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{pharmacyId}/Suspend")
    public ResponseEntity<PharmacyResponseDTO> suspendPharmacy(
            @PathVariable UUID pharmacyId) {
        PharmacyResponseDTO response = pharmacyCreationService.suspendPharmacy(pharmacyId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{pharmacyId}/Unsuspend")
    public ResponseEntity<PharmacyResponseDTO> unsuspendPharmacy(
            @PathVariable UUID pharmacyId) {
        PharmacyResponseDTO response = pharmacyCreationService.unsuspendPharmacy(pharmacyId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/MyPharmacies")
    public ResponseEntity<List<PharmacyResponseDTO>> getMyPharmacies(
            @AuthenticationPrincipal Users currentUser) {
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
    public ResponseEntity<List<PharmacyStaffResponseDTO>> getPharmacyStaffForMembers(
            @PathVariable UUID pharmacyId,
            @AuthenticationPrincipal Users currentUser) {
        List<PharmacyStaffResponseDTO> response = pharmacyCreationService.getPharmacyStaffForMembers(pharmacyId, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{pharmacyId}")
    public ResponseEntity<PharmacyResponseDTO> getPharmacy(
            @PathVariable UUID pharmacyId) {
        PharmacyResponseDTO response = pharmacyCreationService.getPharmacyById(pharmacyId);
        return ResponseEntity.ok(response);
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
    public ResponseEntity<PharmacyResponseDTO> updatePharmacy(
            @PathVariable UUID pharmacyId,
            @Valid @RequestBody PharmacyCreationDTO dto,
            @AuthenticationPrincipal Users currentUser) {
        PharmacyResponseDTO response = pharmacyCreationService.updatePharmacy(pharmacyId, dto, currentUser);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{pharmacyId}/Staff/{staffId}/Suspend")
    public ResponseEntity<PharmacyStaffResponseDTO> suspendStaff(
            @PathVariable UUID pharmacyId,
            @PathVariable UUID staffId,
            @AuthenticationPrincipal Users currentUser) {
        PharmacyStaffResponseDTO response = pharmacyCreationService.suspendStaff(pharmacyId, staffId, currentUser);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{pharmacyId}/Staff/{staffId}/Unsuspend")
    public ResponseEntity<PharmacyStaffResponseDTO> unsuspendStaff(
            @PathVariable UUID pharmacyId,
            @PathVariable UUID staffId,
            @AuthenticationPrincipal Users currentUser) {
        PharmacyStaffResponseDTO response = pharmacyCreationService.unsuspendStaff(pharmacyId, staffId, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/Staff/ByUser/{userId}")
    public ResponseEntity<List<PharmacyStaffResponseDTO>> getStaffByUser(
            @PathVariable UUID userId) {
        List<PharmacyStaffResponseDTO> response = pharmacyCreationService.getStaffByUser(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{pharmacyId}/Staff/{staffId}/ChangeRole")
    public ResponseEntity<PharmacyStaffResponseDTO> changeStaffRole(
            @PathVariable UUID pharmacyId,
            @PathVariable UUID staffId,
            @Valid @RequestBody PharmacyStaffRoleChangeDTO dto,
            @AuthenticationPrincipal Users currentUser) {
        PharmacyStaffResponseDTO response = pharmacyCreationService.changeStaffRole(pharmacyId, staffId, dto.getNewRole(), currentUser);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{pharmacyId}/TransferOwnership")
    public ResponseEntity<PharmacyResponseDTO> transferOwnership(
            @PathVariable UUID pharmacyId,
            @Valid @RequestBody PharmacyTransferDTO dto,
            @AuthenticationPrincipal Users currentUser) {
        PharmacyResponseDTO response = pharmacyCreationService.transferOwnership(pharmacyId, dto.getNewOwnerEmail(), currentUser);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{pharmacyId}/Deactivate")
    public ResponseEntity<PharmacyResponseDTO> deactivatePharmacy(
            @PathVariable UUID pharmacyId,
            @AuthenticationPrincipal Users currentUser) {
        PharmacyResponseDTO response = pharmacyCreationService.deactivatePharmacy(pharmacyId, currentUser);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{pharmacyId}/Reactivate")
    public ResponseEntity<PharmacyResponseDTO> reactivatePharmacy(
            @PathVariable UUID pharmacyId,
            @AuthenticationPrincipal Users currentUser) {
        PharmacyResponseDTO response = pharmacyCreationService.reactivatePharmacy(pharmacyId, currentUser);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{pharmacyId}/Staff/Me")
    public ResponseEntity<Void> removeSelfFromStaff(
            @PathVariable UUID pharmacyId,
            @AuthenticationPrincipal Users currentUser) {
        pharmacyCreationService.removeSelfFromStaff(pharmacyId, currentUser);
        return ResponseEntity.noContent().build();
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
}
