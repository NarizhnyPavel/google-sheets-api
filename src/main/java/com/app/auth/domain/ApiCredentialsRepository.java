package com.app.auth.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApiCredentialsRepository extends JpaRepository<ApiCredentials, Long> {
    Optional<ApiCredentials> findByLogin(String login);
}
