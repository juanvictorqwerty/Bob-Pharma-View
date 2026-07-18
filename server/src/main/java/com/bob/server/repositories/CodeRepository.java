package com.bob.server.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import com.bob.server.model.Code;

public interface CodeRepository extends JpaRepository<Code, UUID> {
}