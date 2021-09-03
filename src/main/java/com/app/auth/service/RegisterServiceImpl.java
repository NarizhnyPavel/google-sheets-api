package com.app.auth.service;

import com.app.api.CustomOAuthTokenInterceptor;
import com.app.api.GoogleInterceptorFactory;
import com.app.api.model.InterceptorCredentials;
import com.app.auth.domain.ApiCredentials;
import com.app.auth.domain.ApiCredentialsRepository;
import com.app.auth.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service

@RequiredArgsConstructor
public class RegisterServiceImpl implements RegisterService {

    private final GoogleInterceptorFactory googleInterceptorFactory;
    private final ApiCredentialsRepository apiCredentialsRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthService authService;

    @Override
    public ResponseEntity<AuthResponse> register(CredentialsDto credentialsDto) {
        Optional<ApiCredentials> apiCredentials = apiCredentialsRepository.findByClientId(credentialsDto.getClientId());
        if (apiCredentials.isPresent()) throw new IllegalStateException("Credentials with this clientId is already exists");
        CustomOAuthTokenInterceptor interceptor = googleInterceptorFactory.getInterceptor(new InterceptorCredentials()
                .setClientId(credentialsDto.getClientId())
                .setClientSecret(credentialsDto.getClientSecret())
                .setRefreshToken(credentialsDto.getRefreshToken()));
        OAuth2AccessToken token = interceptor.getRefreshedToken(new InterceptorCredentials().setClientId(credentialsDto.getClientId())
                .setClientSecret(credentialsDto.getClientSecret())
                .setRefreshToken(credentialsDto.getRefreshToken()));
        if (!token.getScope().contains("https://www.googleapis.com/auth/spreadsheets"))
            throw new BadCredentialsException("Credentials scopes must contain \"https://www.googleapis.com/auth/spreadsheets\"");
        ApiCredentials entity = new ApiCredentials().setClientId(credentialsDto.getClientId())
                .setPassword(passwordEncoder.encode(credentialsDto.getClientSecret()))
                .setClientSecret(credentialsDto.getClientSecret())
                .setRefreshToken(credentialsDto.getRefreshToken());
        ResponseEntity<AuthResponse> authenticate = Optional.of(authService
                .authenticate(new AuthPair(credentialsDto.getClientId(), credentialsDto.getClientSecret())))
                .orElseThrow(() -> new BadCredentialsException("Error while registration"));
        apiCredentialsRepository.save(entity);
        assert authenticate.getBody() != null;
        return ResponseEntity.ok(new AuthResponse().setAuthToken(authenticate.getBody().getAuthToken()));
    }
}
