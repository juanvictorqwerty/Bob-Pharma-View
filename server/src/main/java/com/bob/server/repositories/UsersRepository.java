package com.bob.server.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bob.server.model.Users;

@Repository
public interface UsersRepository extends JpaRepository<Users, UUID> {
    Users findByEmail(String email);
    boolean existsByEmail(String email);
}