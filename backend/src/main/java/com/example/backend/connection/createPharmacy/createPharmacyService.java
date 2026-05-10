package com.example.backend.connection.createPharmacy;

import org.springframework.stereotype.Service;

import com.example.backend.models.Pharmacy;

@Service
public class createPharmacyService {

    public String createPharmacy(Pharmacy pharmacy) {
        return "Pharmacy created successfully";
    }
}
