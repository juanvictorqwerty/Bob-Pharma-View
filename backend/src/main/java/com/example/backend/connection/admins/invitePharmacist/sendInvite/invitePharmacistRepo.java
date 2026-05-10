package com.example.backend.connection.admins.invitePharmacist.sendInvite;

import com.example.backend.models.inviteCodes;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface invitePharmacistRepo extends JpaRepository<inviteCodes, String> {
    Optional<inviteCodes> findByCode(String code);

    Optional<inviteCodes> findByEmail(String email);
}
