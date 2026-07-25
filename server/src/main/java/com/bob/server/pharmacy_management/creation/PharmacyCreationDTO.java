package com.bob.server.pharmacy_management.creation;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PharmacyCreationDTO {

    @NotBlank(message = "Pharmacy name is required")
    private String name;

    @NotBlank(message = "Region is required")
    private String region;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "Latitude is required")
    private String latitude;

    @NotBlank(message = "Longitude is required")
    private String longitude;
}