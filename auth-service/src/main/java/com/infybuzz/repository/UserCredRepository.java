package com.infybuzz.repository;

import com.infybuzz.entity.UserCredEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCredRepository extends JpaRepository<UserCredEntity, Long> {
    Optional<UserCredEntity> findByUsername(String username);
}
