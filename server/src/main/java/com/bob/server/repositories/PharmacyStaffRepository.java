package com.bob.server.repositories;

import java.util.UUID;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.bob.server.model.PharmacyStaff;

public interface PharmacyStaffRepository extends JpaRepository<PharmacyStaff, UUID> {
    List<PharmacyStaff> findByUserId(UUID userId);
    List<PharmacyStaff> findByPharmacyId(UUID pharmacyId);
    boolean existsByUserIdAndPharmacyId(UUID userId, UUID pharmacyId);
}