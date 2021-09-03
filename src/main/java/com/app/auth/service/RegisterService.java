package com.app.auth.service;

import com.app.auth.model.AuthResponse;
import com.app.auth.model.CredentialsDto;
import org.springframework.http.ResponseEntity;

public interface RegisterService {
    ResponseEntity<AuthResponse> register(CredentialsDto credentialsDto);
}
