package com.bob.server.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import com.bob.server.model.Code;

public interface CodeRepository extends JpaRepository<Code, UUID> {
    Optional<Code> findByCode(String code);
    Optional<Code> findByCodeIgnoreCase(String code);
    boolean existsByEmail(String email);
    Optional<Code> findByEmailAndCategoryAndUsedFalse(String email, String category);
    Optional<Code> findByEmailAndCodeAndCategory(String email, String code, String category);
}
