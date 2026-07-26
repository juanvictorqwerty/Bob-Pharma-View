package com.bob.server.repositories;

import java.util.List;
import java.util.UUID;

import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.bob.server.model.Pharmacy;

public interface PharmacyRepository extends JpaRepository<Pharmacy, UUID> {
       boolean existsByNameAndRegionAndCity(String name, String region, String city);
       
       List<Pharmacy> findByNameContainingIgnoreCase(String name);
       List<Pharmacy> findByRegion(String region);
       List<Pharmacy> findByCity(String city);
       List<Pharmacy> findByIsApproved(boolean isApproved);
       List<Pharmacy> findByIsActive(boolean isActive);
       List<Pharmacy> findByRegionAndCity(String region, String city);
       
    @Query("SELECT p FROM Pharmacy p WHERE " +
           "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:region IS NULL OR p.region = :region) AND " +
           "(:city IS NULL OR p.city = :city)")
    Page<Pharmacy> searchPharmacies(@Param("name") String name,
                                    @Param("region") String region,
                                    @Param("city") String city,
                                    Pageable pageable);

    @Query(value = "SELECT * FROM pharmacy p WHERE p.location IS NOT NULL AND " +
              "ST_DWithin(p.location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326), :distanceInMeters)", 
              nativeQuery = true)
       List<Pharmacy> findNearbyPharmacies(@Param("latitude") double latitude, 
                                          @Param("longitude") double longitude, 
                                          @Param("distanceInMeters") double distanceInMeters);
}
