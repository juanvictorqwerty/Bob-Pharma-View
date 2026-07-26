package com.bob.server.pharmacy_management.creation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bob.server.model.Pharmacy;
import com.bob.server.model.PharmacyStaff;
import com.bob.server.model.Users;
import com.bob.server.repositories.PharmacyRepository;
import com.bob.server.repositories.PharmacyStaffRepository;
import com.bob.server.repositories.UsersRepository;

@Service
public class PharmacyCreationService {

    private final PharmacyRepository pharmacyRepository;
    private final PharmacyStaffRepository pharmacyStaffRepository;
    private final UsersRepository usersRepository;

    public PharmacyCreationService(PharmacyRepository pharmacyRepository,
                                    PharmacyStaffRepository pharmacyStaffRepository,
                                    UsersRepository usersRepository) {
        this.pharmacyRepository = pharmacyRepository;
        this.pharmacyStaffRepository = pharmacyStaffRepository;
        this.usersRepository = usersRepository;
    }

    @Transactional
    public PharmacyResponseDTO createPharmacy(PharmacyCreationDTO dto, Users creator) {
        if (pharmacyRepository.existsByNameAndRegionAndCity(dto.getName(), dto.getRegion(), dto.getCity())) {
            throw new PharmacyCreationException(PharmacyCreationValidation.PHARMACY_ALREADY_EXISTS.getMessage());
        }

        Pharmacy pharmacy = new Pharmacy();
        pharmacy.setName(dto.getName());
        pharmacy.setRegion(dto.getRegion());
        pharmacy.setCity(dto.getCity());
        pharmacy.setLatitude(dto.getLatitude());
        pharmacy.setLongitude(dto.getLongitude());
        pharmacy.setCreatorId(creator);
        pharmacy.setApproved(false);
        pharmacy.setSuspended(false);
        pharmacy.setActive(true);
        pharmacy.setCreatedAt(Instant.now().toString());
        pharmacy.setUpdatedAt(Instant.now().toString());

        // Set location point from lat/lng
        try {
            double lat = Double.parseDouble(dto.getLatitude());
            double lng = Double.parseDouble(dto.getLongitude());
            GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
            Point point = geometryFactory.createPoint(new Coordinate(lng, lat));
            pharmacy.setLocation(point);
        } catch (NumberFormatException e) {
            throw new PharmacyCreationException("Invalid latitude or longitude values");
        }

        Pharmacy saved = pharmacyRepository.save(pharmacy);
        return mapToResponseDTO(saved);
    }

    @Transactional
    public PharmacyResponseDTO approvePharmacy(UUID pharmacyId) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new PharmacyCreationException(PharmacyCreationValidation.PHARMACY_NOT_FOUND.getMessage()));

        if (pharmacy.isApproved()) {
            throw new PharmacyCreationException(PharmacyCreationValidation.PHARMACY_ALREADY_APPROVED.getMessage());
        }

        pharmacy.setApproved(true);
        pharmacy.setUpdatedAt(Instant.now().toString());
        Pharmacy saved = pharmacyRepository.save(pharmacy);
        return mapToResponseDTO(saved);
    }

    @Transactional
    public PharmacyStaffResponseDTO addStaff(UUID pharmacyId, PharmacyStaffAssignmentDTO dto, Users currentUser) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new PharmacyCreationException(PharmacyCreationValidation.PHARMACY_NOT_FOUND.getMessage()));

        checkPharmacyUsable(pharmacy);

        // Validate role
        String role = dto.getRole().toUpperCase();
        if (!role.equals("PHARMACY_ADMIN") && !role.equals("PHARMACY_PERSONNEL")) {
            throw new PharmacyCreationException(PharmacyCreationValidation.INVALID_ROLE.getMessage());
        }

        // Check authorization
        boolean isCreator = pharmacy.getCreatorId().getID().equals(currentUser.getID());
        if (role.equals("PHARMACY_ADMIN")) {
            // Only the creator can add pharmacy admins
            if (!isCreator) {
                throw new PharmacyCreationException(PharmacyCreationValidation.UNAUTHORIZED_ACTION.getMessage());
            }
        } else {
            // Creator or pharmacy admin can add personnel
            if (!isCreator && !isPharmacyAdmin(currentUser, pharmacy)) {
                throw new PharmacyCreationException(PharmacyCreationValidation.UNAUTHORIZED_ACTION.getMessage());
            }
        }

        // Find user by email
        Users userToAssign = usersRepository.findByEmail(dto.getEmail());
        if (userToAssign == null) {
            throw new PharmacyCreationException(PharmacyCreationValidation.USER_NOT_FOUND.getMessage());
        }

        // Check if already staff
        if (pharmacyStaffRepository.existsByUserIdAndPharmacyId(userToAssign.getID(), pharmacyId)) {
            throw new PharmacyCreationException(PharmacyCreationValidation.USER_ALREADY_STAFF.getMessage());
        }

        PharmacyStaff staff = new PharmacyStaff();
        staff.setUserId(userToAssign);
        staff.setPharmacyId(pharmacy);
        staff.setRole(role);
        staff.setCreatedAt(Instant.now().toString());
        staff.setUpdatedAt(Instant.now().toString());
        staff.setSuspended(false);

        PharmacyStaff saved = pharmacyStaffRepository.save(staff);
        return mapToStaffResponseDTO(saved, userToAssign.getEmail());
    }

    @Transactional
    public void removeStaff(UUID pharmacyId, UUID staffId, Users currentUser) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new PharmacyCreationException(PharmacyCreationValidation.PHARMACY_NOT_FOUND.getMessage()));

        PharmacyStaff staff = pharmacyStaffRepository.findById(staffId)
                .orElseThrow(() -> new PharmacyCreationException(PharmacyCreationValidation.STAFF_NOT_FOUND.getMessage()));

        if (!staff.getPharmacyId().getID().equals(pharmacyId)) {
            throw new PharmacyCreationException(PharmacyCreationValidation.STAFF_NOT_FOUND.getMessage());
        }

        // Cannot remove the creator
        if (staff.getUserId().getID().equals(pharmacy.getCreatorId().getID())) {
            throw new PharmacyCreationException(PharmacyCreationValidation.CANNOT_REMOVE_CREATOR.getMessage());
        }

        boolean isCreator = pharmacy.getCreatorId().getID().equals(currentUser.getID());
        boolean isAdmin = isPharmacyAdmin(currentUser, pharmacy);

        if (isCreator) {
            // Creator can remove anyone (except themselves, already handled above)
            pharmacyStaffRepository.delete(staff);
        } else if (isAdmin) {
            // Pharmacy admin can remove other admins (except creator) and personnel
            PharmacyStaff currentUserStaff = pharmacyStaffRepository
                    .findByUserIdAndPharmacyId(currentUser.getID(), pharmacyId)
                    .stream()
                    .findFirst()
                    .orElse(null);
            if (currentUserStaff != null && currentUserStaff.getID().equals(staffId)) {
                throw new PharmacyCreationException("You cannot remove yourself");
            }
            pharmacyStaffRepository.delete(staff);
        } else {
            throw new PharmacyCreationException(PharmacyCreationValidation.UNAUTHORIZED_ACTION.getMessage());
        }
    }

    @Transactional
    public PharmacyResponseDTO suspendPharmacy(UUID pharmacyId) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new PharmacyCreationException(PharmacyCreationValidation.PHARMACY_NOT_FOUND.getMessage()));

        pharmacy.setSuspended(true);
        pharmacy.setActive(false);
        pharmacy.setUpdatedAt(Instant.now().toString());
        Pharmacy saved = pharmacyRepository.save(pharmacy);
        return mapToResponseDTO(saved);
    }

    @Transactional
    public PharmacyResponseDTO unsuspendPharmacy(UUID pharmacyId) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new PharmacyCreationException(PharmacyCreationValidation.PHARMACY_NOT_FOUND.getMessage()));

        pharmacy.setSuspended(false);
        pharmacy.setActive(true);
        pharmacy.setUpdatedAt(Instant.now().toString());
        Pharmacy saved = pharmacyRepository.save(pharmacy);
        return mapToResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public PharmacyResponseDTO getPharmacyById(UUID pharmacyId) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new PharmacyCreationException(PharmacyCreationValidation.PHARMACY_NOT_FOUND.getMessage()));
        return mapToResponseDTO(pharmacy);
    }

    @Transactional(readOnly = true)
    public Page<PharmacyResponseDTO> searchPharmacies(String name, String region, String city, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Pharmacy> pharmacyPage = pharmacyRepository.searchPharmacies(name, region, city, pageable);
        return pharmacyPage.map(this::mapToResponseDTO);
    }

    @Transactional(readOnly = true)
    public List<PharmacyResponseDTO> getPharmacies(boolean onlyApproved, String region, String city) {
        List<Pharmacy> pharmacies;

        if (region != null && city != null) {
            pharmacies = pharmacyRepository.findByRegionAndCity(region, city);
        } else if (region != null) {
            pharmacies = pharmacyRepository.findByRegion(region);
        } else if (city != null) {
            pharmacies = pharmacyRepository.findByCity(city);
        } else if (onlyApproved) {
            pharmacies = pharmacyRepository.findByIsApproved(true);
        } else {
            pharmacies = pharmacyRepository.findAll();
        }

        return pharmacies.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PharmacyStaffResponseDTO> getPharmacyStaff(UUID pharmacyId) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new PharmacyCreationException(PharmacyCreationValidation.PHARMACY_NOT_FOUND.getMessage()));

        checkPharmacyUsable(pharmacy);

        List<PharmacyStaff> staffList = pharmacyStaffRepository.findByPharmacyId(pharmacyId);
        return staffList.stream()
                .map(s -> mapToStaffResponseDTO(s, s.getUserId().getEmail()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PharmacyStaffResponseDTO> getPharmacyStaffForMembers(UUID pharmacyId, Users currentUser) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new PharmacyCreationException(PharmacyCreationValidation.PHARMACY_NOT_FOUND.getMessage()));

        // Check if user is creator or staff member of this pharmacy
        boolean isCreator = pharmacy.getCreatorId().getID().equals(currentUser.getID());
        boolean isStaff = pharmacyStaffRepository.existsByUserIdAndPharmacyId(currentUser.getID(), pharmacyId);

        if (!isCreator && !isStaff) {
            throw new PharmacyCreationException(PharmacyCreationValidation.UNAUTHORIZED_ACTION.getMessage());
        }

        List<PharmacyStaff> staffList = pharmacyStaffRepository.findByPharmacyId(pharmacyId);
        return staffList.stream()
                .map(s -> mapToStaffResponseDTO(s, s.getUserId().getEmail()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PharmacyResponseDTO> getMyPharmacies(Users currentUser) {
        List<Pharmacy> pharmacies = new ArrayList<>();

        // Find pharmacies where user is the creator
        List<Pharmacy> allPharmacies = pharmacyRepository.findAll();
        for (Pharmacy pharmacy : allPharmacies) {
            if (pharmacy.getCreatorId().getID().equals(currentUser.getID())) {
                pharmacies.add(pharmacy);
            }
        }

        // Find pharmacies where user is a staff member
        List<PharmacyStaff> staffRecords = pharmacyStaffRepository.findByUserId(currentUser.getID());
        for (PharmacyStaff staff : staffRecords) {
            Pharmacy pharmacy = staff.getPharmacyId();
            if (pharmacies.stream().noneMatch(p -> p.getID().equals(pharmacy.getID()))) {
                pharmacies.add(pharmacy);
            }
        }

        return pharmacies.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public PharmacyResponseDTO updatePharmacy(UUID pharmacyId, PharmacyCreationDTO dto, Users currentUser) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new PharmacyCreationException(PharmacyCreationValidation.PHARMACY_NOT_FOUND.getMessage()));

        // Only creator or PHARMACY_ADMIN can update
        boolean isCreator = pharmacy.getCreatorId().getID().equals(currentUser.getID());
        boolean isAdmin = isPharmacyAdmin(currentUser, pharmacy);
        if (!isCreator && !isAdmin) {
            throw new PharmacyCreationException(PharmacyCreationValidation.UNAUTHORIZED_ACTION.getMessage());
        }

        // Check uniqueness if name, region, or city changed
        boolean nameChanged = !pharmacy.getName().equals(dto.getName());
        boolean regionChanged = !pharmacy.getRegion().equals(dto.getRegion());
        boolean cityChanged = !pharmacy.getCity().equals(dto.getCity());
        if (nameChanged || regionChanged || cityChanged) {
            if (pharmacyRepository.existsByNameAndRegionAndCity(dto.getName(), dto.getRegion(), dto.getCity())) {
                throw new PharmacyCreationException(PharmacyCreationValidation.PHARMACY_ALREADY_EXISTS.getMessage());
            }
        }

        pharmacy.setName(dto.getName());
        pharmacy.setRegion(dto.getRegion());
        pharmacy.setCity(dto.getCity());
        pharmacy.setLatitude(dto.getLatitude());
        pharmacy.setLongitude(dto.getLongitude());
        pharmacy.setUpdatedAt(Instant.now().toString());

        // Update location point
        try {
            double lat = Double.parseDouble(dto.getLatitude());
            double lng = Double.parseDouble(dto.getLongitude());
            GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
            Point point = geometryFactory.createPoint(new Coordinate(lng, lat));
            pharmacy.setLocation(point);
        } catch (NumberFormatException e) {
            throw new PharmacyCreationException("Invalid latitude or longitude values");
        }

        Pharmacy saved = pharmacyRepository.save(pharmacy);
        return mapToResponseDTO(saved);
    }

    @Transactional
    public PharmacyStaffResponseDTO suspendStaff(UUID pharmacyId, UUID staffId, Users currentUser) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new PharmacyCreationException(PharmacyCreationValidation.PHARMACY_NOT_FOUND.getMessage()));

        PharmacyStaff staff = pharmacyStaffRepository.findById(staffId)
                .orElseThrow(() -> new PharmacyCreationException(PharmacyCreationValidation.STAFF_NOT_FOUND.getMessage()));

        if (!staff.getPharmacyId().getID().equals(pharmacyId)) {
            throw new PharmacyCreationException(PharmacyCreationValidation.STAFF_NOT_FOUND.getMessage());
        }

        // Cannot suspend the creator
        if (staff.getUserId().getID().equals(pharmacy.getCreatorId().getID())) {
            throw new PharmacyCreationException(PharmacyCreationValidation.CANNOT_SUSPEND_CREATOR.getMessage());
        }

        // Check authorization: creator or PHARMACY_ADMIN
        boolean isCreator = pharmacy.getCreatorId().getID().equals(currentUser.getID());
        boolean isAdmin = isPharmacyAdmin(currentUser, pharmacy);
        if (!isCreator && !isAdmin) {
            throw new PharmacyCreationException(PharmacyCreationValidation.UNAUTHORIZED_ACTION.getMessage());
        }

        if (staff.isSuspended()) {
            throw new PharmacyCreationException(PharmacyCreationValidation.STAFF_ALREADY_SUSPENDED.getMessage());
        }

        staff.setSuspended(true);
        staff.setUpdatedAt(Instant.now().toString());
        PharmacyStaff saved = pharmacyStaffRepository.save(staff);
        return mapToStaffResponseDTO(saved, saved.getUserId().getEmail());
    }

    @Transactional
    public PharmacyStaffResponseDTO unsuspendStaff(UUID pharmacyId, UUID staffId, Users currentUser) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new PharmacyCreationException(PharmacyCreationValidation.PHARMACY_NOT_FOUND.getMessage()));

        PharmacyStaff staff = pharmacyStaffRepository.findById(staffId)
                .orElseThrow(() -> new PharmacyCreationException(PharmacyCreationValidation.STAFF_NOT_FOUND.getMessage()));

        if (!staff.getPharmacyId().getID().equals(pharmacyId)) {
            throw new PharmacyCreationException(PharmacyCreationValidation.STAFF_NOT_FOUND.getMessage());
        }

        // Check authorization: creator or PHARMACY_ADMIN
        boolean isCreator = pharmacy.getCreatorId().getID().equals(currentUser.getID());
        boolean isAdmin = isPharmacyAdmin(currentUser, pharmacy);
        if (!isCreator && !isAdmin) {
            throw new PharmacyCreationException(PharmacyCreationValidation.UNAUTHORIZED_ACTION.getMessage());
        }

        if (!staff.isSuspended()) {
            throw new PharmacyCreationException(PharmacyCreationValidation.STAFF_NOT_SUSPENDED.getMessage());
        }

        staff.setSuspended(false);
        staff.setUpdatedAt(Instant.now().toString());
        PharmacyStaff saved = pharmacyStaffRepository.save(staff);
        return mapToStaffResponseDTO(saved, saved.getUserId().getEmail());
    }

    @Transactional(readOnly = true)
    public List<PharmacyStaffResponseDTO> getStaffByUser(UUID userId) {
        List<PharmacyStaff> staffList = pharmacyStaffRepository.findByUserId(userId);
        return staffList.stream()
                .map(s -> mapToStaffResponseDTO(s, s.getUserId().getEmail()))
                .collect(Collectors.toList());
    }

    @Transactional
    public PharmacyStaffResponseDTO changeStaffRole(UUID pharmacyId, UUID staffId, String newRole, Users currentUser) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new PharmacyCreationException(PharmacyCreationValidation.PHARMACY_NOT_FOUND.getMessage()));

        PharmacyStaff staff = pharmacyStaffRepository.findById(staffId)
                .orElseThrow(() -> new PharmacyCreationException(PharmacyCreationValidation.STAFF_NOT_FOUND.getMessage()));

        if (!staff.getPharmacyId().getID().equals(pharmacyId)) {
            throw new PharmacyCreationException(PharmacyCreationValidation.STAFF_NOT_FOUND.getMessage());
        }

        // Cannot change creator's role
        if (staff.getUserId().getID().equals(pharmacy.getCreatorId().getID())) {
            throw new PharmacyCreationException(PharmacyCreationValidation.CANNOT_CHANGE_CREATOR_ROLE.getMessage());
        }

        // Validate new role
        String role = newRole.toUpperCase();
        if (!role.equals("PHARMACY_ADMIN") && !role.equals("PHARMACY_PERSONNEL")) {
            throw new PharmacyCreationException(PharmacyCreationValidation.INVALID_ROLE.getMessage());
        }

        // Check authorization: creator or PHARMACY_ADMIN
        boolean isCreator = pharmacy.getCreatorId().getID().equals(currentUser.getID());
        boolean isAdmin = isPharmacyAdmin(currentUser, pharmacy);
        if (!isCreator && !isAdmin) {
            throw new PharmacyCreationException(PharmacyCreationValidation.UNAUTHORIZED_ACTION.getMessage());
        }

        staff.setRole(role);
        staff.setUpdatedAt(Instant.now().toString());
        PharmacyStaff saved = pharmacyStaffRepository.save(staff);
        return mapToStaffResponseDTO(saved, saved.getUserId().getEmail());
    }

    @Transactional
    public PharmacyResponseDTO transferOwnership(UUID pharmacyId, String newOwnerEmail, Users currentUser) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new PharmacyCreationException(PharmacyCreationValidation.PHARMACY_NOT_FOUND.getMessage()));

        // Only current creator can transfer
        if (!pharmacy.getCreatorId().getID().equals(currentUser.getID())) {
            throw new PharmacyCreationException(PharmacyCreationValidation.UNAUTHORIZED_ACTION.getMessage());
        }

        // Cannot transfer to self
        if (currentUser.getEmail().equalsIgnoreCase(newOwnerEmail)) {
            throw new PharmacyCreationException(PharmacyCreationValidation.CANNOT_TRANSFER_TO_SELF.getMessage());
        }

        // Find new owner
        Users newOwner = usersRepository.findByEmail(newOwnerEmail);
        if (newOwner == null) {
            throw new PharmacyCreationException(PharmacyCreationValidation.USER_NOT_FOUND.getMessage());
        }

        pharmacy.setCreatorId(newOwner);
        pharmacy.setUpdatedAt(Instant.now().toString());
        Pharmacy saved = pharmacyRepository.save(pharmacy);
        return mapToResponseDTO(saved);
    }

    @Transactional
    public PharmacyResponseDTO deactivatePharmacy(UUID pharmacyId, Users currentUser) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new PharmacyCreationException(PharmacyCreationValidation.PHARMACY_NOT_FOUND.getMessage()));

        // Only creator or PHARMACY_ADMIN can deactivate
        boolean isCreator = pharmacy.getCreatorId().getID().equals(currentUser.getID());
        boolean isAdmin = isPharmacyAdmin(currentUser, pharmacy);
        if (!isCreator && !isAdmin) {
            throw new PharmacyCreationException(PharmacyCreationValidation.UNAUTHORIZED_ACTION.getMessage());
        }

        if (!pharmacy.isActive()) {
            throw new PharmacyCreationException(PharmacyCreationValidation.PHARMACY_ALREADY_INACTIVE.getMessage());
        }

        pharmacy.setActive(false);
        pharmacy.setUpdatedAt(Instant.now().toString());
        Pharmacy saved = pharmacyRepository.save(pharmacy);
        return mapToResponseDTO(saved);
    }

    @Transactional
    public PharmacyResponseDTO reactivatePharmacy(UUID pharmacyId, Users currentUser) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new PharmacyCreationException(PharmacyCreationValidation.PHARMACY_NOT_FOUND.getMessage()));

        // Only creator or PHARMACY_ADMIN can reactivate
        boolean isCreator = pharmacy.getCreatorId().getID().equals(currentUser.getID());
        boolean isAdmin = isPharmacyAdmin(currentUser, pharmacy);
        if (!isCreator && !isAdmin) {
            throw new PharmacyCreationException(PharmacyCreationValidation.UNAUTHORIZED_ACTION.getMessage());
        }

        if (pharmacy.isActive()) {
            throw new PharmacyCreationException(PharmacyCreationValidation.PHARMACY_ALREADY_ACTIVE.getMessage());
        }

        pharmacy.setActive(true);
        pharmacy.setUpdatedAt(Instant.now().toString());
        Pharmacy saved = pharmacyRepository.save(pharmacy);
        return mapToResponseDTO(saved);
    }

    @Transactional
    public void removeSelfFromStaff(UUID pharmacyId, Users currentUser) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new PharmacyCreationException(PharmacyCreationValidation.PHARMACY_NOT_FOUND.getMessage()));

        // Creator cannot remove themselves
        if (pharmacy.getCreatorId().getID().equals(currentUser.getID())) {
            throw new PharmacyCreationException(PharmacyCreationValidation.CANNOT_REMOVE_SELF_AS_CREATOR.getMessage());
        }

        List<PharmacyStaff> staffRecords = pharmacyStaffRepository.findByUserIdAndPharmacyId(currentUser.getID(), pharmacyId);
        if (staffRecords.isEmpty()) {
            throw new PharmacyCreationException(PharmacyCreationValidation.STAFF_NOT_FOUND.getMessage());
        }

        pharmacyStaffRepository.deleteAll(staffRecords);
    }

    @Transactional(readOnly = true)
    public long getPharmacyStaffCount(UUID pharmacyId) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new PharmacyCreationException(PharmacyCreationValidation.PHARMACY_NOT_FOUND.getMessage()));

        return pharmacyStaffRepository.findByPharmacyId(pharmacyId).size();
    }

    @Transactional(readOnly = true)
    public List<PharmacyResponseDTO> findNearbyPharmacies(double latitude, double longitude, double distanceInMeters) {
        List<Pharmacy> pharmacies = pharmacyRepository.findNearbyPharmacies(latitude, longitude, distanceInMeters);
        return pharmacies.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // --- Helper methods ---

    private void checkPharmacyUsable(Pharmacy pharmacy) {
        if (!pharmacy.isApproved()) {
            throw new PharmacyCreationException(PharmacyCreationValidation.PHARMACY_NOT_APPROVED.getMessage());
        }
        if (pharmacy.isSuspended()) {
            throw new PharmacyCreationException(PharmacyCreationValidation.PHARMACY_SUSPENDED.getMessage());
        }
        if (!pharmacy.isActive()) {
            throw new PharmacyCreationException(PharmacyCreationValidation.PHARMACY_NOT_ACTIVE.getMessage());
        }
    }

    private boolean isPharmacyAdmin(Users user, Pharmacy pharmacy) {
        List<PharmacyStaff> staffList = pharmacyStaffRepository.findByUserIdAndPharmacyId(user.getID(), pharmacy.getID());
        return staffList.stream().anyMatch(s -> s.getRole().equals("PHARMACY_ADMIN"));
    }

    private PharmacyResponseDTO mapToResponseDTO(Pharmacy pharmacy) {
        PharmacyResponseDTO dto = new PharmacyResponseDTO();
        dto.setId(pharmacy.getID());
        dto.setName(pharmacy.getName());
        dto.setRegion(pharmacy.getRegion());
        dto.setCity(pharmacy.getCity());
        dto.setLatitude(pharmacy.getLatitude());
        dto.setLongitude(pharmacy.getLongitude());
        dto.setCreatedAt(pharmacy.getCreatedAt());
        dto.setUpdatedAt(pharmacy.getUpdatedAt());
        dto.setCreatorId(pharmacy.getCreatorId() != null ? pharmacy.getCreatorId().getID() : null);
        dto.setApproved(pharmacy.isApproved());
        dto.setSuspended(pharmacy.isSuspended());
        dto.setActive(pharmacy.isActive());
        return dto;
    }

    private PharmacyStaffResponseDTO mapToStaffResponseDTO(PharmacyStaff staff, String userEmail) {
        PharmacyStaffResponseDTO dto = new PharmacyStaffResponseDTO();
        dto.setId(staff.getID());
        dto.setUserId(staff.getUserId().getID());
        dto.setUserEmail(userEmail);
        dto.setPharmacyId(staff.getPharmacyId().getID());
        dto.setRole(staff.getRole());
        dto.setCreatedAt(staff.getCreatedAt());
        dto.setUpdatedAt(staff.getUpdatedAt());
        dto.setSuspended(staff.isSuspended());
        return dto;
    }
}