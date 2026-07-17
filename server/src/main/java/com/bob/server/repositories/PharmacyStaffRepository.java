package com.bob.server.repositories;

import java.util.UUID;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bob.server.model.PharmacyStaff;

@Repository
public interface PharmacyStaffRepository extends JpaRepository<PharmacyStaff, UUID> {
    List<PharmacyStaff> findByUserId(UUID userId);
    List<PharmacyStaff> findByPharmacyId(UUID pharmacyId);
    boolean existsByUserIdAndPharmacyId(UUID userId, UUID pharmacyId);
}