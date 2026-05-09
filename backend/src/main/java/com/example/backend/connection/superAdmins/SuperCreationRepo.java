package com.example.backend.connection.superAdmins;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.models.Users;

public interface SuperCreationRepo extends JpaRepository<Users, String> {

}
