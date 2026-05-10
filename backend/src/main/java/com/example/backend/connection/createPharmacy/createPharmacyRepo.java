package com.example.backend.connection.createPharmacy;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.models.Pharmacy;

public interface createPharmacyRepo extends JpaRepository<Pharmacy, String> {
    public Pharmacy findBypharmacyId(String pharmacyId);

    public Pharmacy findBypharmacyName(String pharmacyName);
}
