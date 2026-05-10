package com.example.backend.connection.createPharmacy;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.models.Pharmacy;

@RestController
@RequestMapping("/createPharmacy")
public class createPharmacyController {

    @PostMapping
    public String createPharmacy(@RequestBody Pharmacy pharmacy) {
        return "Pharmacy created successfully";
    }
}
