package com.example.backend.connection.admins;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.backend.models.Users;

@Repository
public interface acceptInvitationRepo extends JpaRepository<Users, String> {
    boolean existsByEmail(String email);
}
