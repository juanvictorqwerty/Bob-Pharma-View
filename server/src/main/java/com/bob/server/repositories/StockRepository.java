package com.bob.server.repositories;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.bob.server.model.Stock;

public interface StockRepository extends JpaRepository<Stock, UUID> {
    List<Stock> findByPharmacyId(UUID pharmacyId);
    List<Stock> findByDrugId(UUID drugId);
    Optional<Stock> findByPharmacyIdAndDrugId(UUID pharmacyId, UUID drugId);
}