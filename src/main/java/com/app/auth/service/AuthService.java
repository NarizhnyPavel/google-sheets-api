package com.app.auth.service;

import com.app.api.model.InterceptorCredentials;
import com.app.auth.domain.ApiCredentials;
import com.app.auth.model.AuthPair;
import com.app.auth.model.AuthResponse;
import com.app.auth.model.CredentialsDto;
import org.springframework.http.ResponseEntity;

public interface AuthService {

    ResponseEntity<AuthResponse> authenticate(AuthPair authPair);

    InterceptorCredentials getCredentialsForCurrentUser();

}
