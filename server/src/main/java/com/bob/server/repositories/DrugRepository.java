package com.bob.server.repositories;

import java.util.UUID;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.bob.server.model.Drug;

public interface DrugRepository extends JpaRepository<Drug, UUID> {
    Optional<Drug> findByName(String name);
    boolean existsByName(String name);
}