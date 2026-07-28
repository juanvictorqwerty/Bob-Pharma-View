package com.bob.server.repositories;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.bob.server.model.Stock;

public interface StockRepository extends JpaRepository<Stock, UUID> {
    List<Stock> findByPharmacyId(UUID pharmacyId);
    List<Stock> findByDrugId(UUID drugId);
    Optional<Stock> findByPharmacyIdAndDrugId(UUID pharmacyId, UUID drugId);

    @Query(value = "SELECT s.id AS stockId, s.quantity, s.updated_at AS stockUpdatedAt, " +
            "d.id AS drugId, d.name AS drugName, " +
            "p.id AS pharmacyId, p.name AS pharmacyName, p.region, p.city, " +
            "CAST(p.latitude AS double precision) AS latitude, CAST(p.longitude AS double precision) AS longitude, " +
            "ST_DistanceSphere(p.location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)) AS distanceMeters " +
            "FROM stock s " +
            "JOIN drug d ON d.id = s.drug_id " +
            "JOIN pharmacy p ON p.id = s.pharmacy_id " +
            "WHERE p.is_approved = true AND p.is_suspended = false AND p.is_active = true " +
            "AND s.quantity > 0 " +
            "AND (LOWER(d.name) = LOWER(:query) " +
            "     OR LOWER(d.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "     OR d.name % :query) " +
            "AND ST_DWithin(p.location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326), :radiusMeters) " +
            "ORDER BY " +
            "  CASE WHEN LOWER(d.name) = LOWER(:query) THEN 0 ELSE 1 END, " +
            "  similarity(d.name, :query) DESC",
            nativeQuery = true)
        List<Object[]> searchDrugsGeo(@Param("query") String query,
                                    @Param("latitude") double latitude,
                                    @Param("longitude") double longitude,
                                    @Param("radiusMeters") double radiusMeters);

        @Query(value = "SELECT s.id AS stockId, s.quantity, s.updated_at AS stockUpdatedAt, " +
            "d.id AS drugId, d.name AS drugName, " +
            "p.id AS pharmacyId, p.name AS pharmacyName, p.region, p.city, " +
            "CAST(p.latitude AS double precision) AS latitude, CAST(p.longitude AS double precision) AS longitude " +
            "FROM stock s " +
            "JOIN drug d ON d.id = s.drug_id " +
            "JOIN pharmacy p ON p.id = s.pharmacy_id " +
            "WHERE p.is_approved = true AND p.is_suspended = false AND p.is_active = true " +
            "AND s.quantity > 0 " +
            "AND (LOWER(d.name) = LOWER(:query) " +
            "     OR LOWER(d.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "     OR d.name % :query) " +
            "ORDER BY " +
            "  CASE WHEN LOWER(d.name) = LOWER(:query) THEN 0 ELSE 1 END, " +
            "  similarity(d.name, :query) DESC",
            nativeQuery = true)
        List<Object[]> searchDrugsNational(@Param("query") String query);
}
