package com.bob.server.repositories;

import java.util.UUID;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.bob.server.model.PharmacyStaff;

public interface PharmacyStaffRepository extends JpaRepository<PharmacyStaff, UUID> {
    @Query("SELECT ps FROM PharmacyStaff ps WHERE ps.userId.ID = :userId")
    List<PharmacyStaff> findByUserId(@Param("userId") UUID userId);

    @Query("SELECT ps FROM PharmacyStaff ps WHERE ps.pharmacyId.ID = :pharmacyId")
    List<PharmacyStaff> findByPharmacyId(@Param("pharmacyId") UUID pharmacyId);

    @Query("SELECT ps FROM PharmacyStaff ps WHERE ps.userId.ID = :userId AND ps.pharmacyId.ID = :pharmacyId")
    List<PharmacyStaff> findByUserIdAndPharmacyId(@Param("userId") UUID userId, @Param("pharmacyId") UUID pharmacyId);

    @Query("SELECT CASE WHEN COUNT(ps) > 0 THEN true ELSE false END FROM PharmacyStaff ps WHERE ps.userId.ID = :userId AND ps.pharmacyId.ID = :pharmacyId")
    boolean existsByUserIdAndPharmacyId(@Param("userId") UUID userId, @Param("pharmacyId") UUID pharmacyId);
}
