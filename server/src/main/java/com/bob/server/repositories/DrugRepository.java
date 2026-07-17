package com.bob.server.repositories;

import java.util.UUID;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bob.server.model.Drug;

@Repository
public interface DrugRepository extends JpaRepository<Drug, UUID> {
    Optional<Drug> findByName(String name);
    boolean existsByName(String name);
}