package com.example.backend.connection.admins;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface sendingInviteRepo<Invite> extends JpaRepository<Invite, Integer> {
    Optional<Invite> findByEmail(String email);
}
