package com.example.backend.connection.admins.invitePharmacist.acceptInvite;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

import com.example.backend.models.Users;

public interface acceptInviteRepo extends JpaRepository<Users, String> {
    Optional<Users> findByCode(String code);

    Optional<Users> findByEmail(String email);
}
