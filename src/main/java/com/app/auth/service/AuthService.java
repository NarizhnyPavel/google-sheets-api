package com.app.auth.service;

import com.app.api.InterceptorCredentials;
import com.app.auth.model.AuthRequest;
import com.app.auth.model.AuthResponse;
import com.app.auth.model.TokenResponse;
import org.springframework.http.ResponseEntity;

public interface AuthService {

    ResponseEntity<AuthResponse> authenticate(AuthRequest authRequest);

    ResponseEntity<TokenResponse> getRefreshToken(AuthRequest authRequest);

    InterceptorCredentials getCredentialsForCurrentUser();

}
