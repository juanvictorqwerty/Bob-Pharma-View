package com.example.backend.connection.superAdmins;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.models.Users;

import java.util.Optional;

public interface SuperCreationRepo extends JpaRepository<Users, String> {

    boolean existsByEmail(String email);

    Optional<Users> findByEmail(String email);
}
