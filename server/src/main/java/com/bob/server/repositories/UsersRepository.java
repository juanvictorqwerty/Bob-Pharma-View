package com.bob.server.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import com.bob.server.model.Users;

public interface UsersRepository extends JpaRepository<Users, UUID> {
    Users findByEmail(String email);
    boolean existsByEmail(String email);
}