package com.app.auth.service;

import com.app.api.model.InterceptorCredentials;
import com.app.auth.domain.ApiCredentials;
import com.app.auth.domain.ApiCredentialsRepository;
import com.app.auth.model.AuthPair;
import com.app.auth.model.AuthResponse;
import com.app.auth.model.CredentialsDto;
import com.app.auth.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;

@Service

@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{

    private final ApiCredentialsRepository apiCredentialsRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Override
    public ResponseEntity<AuthResponse> authenticate(AuthPair authPair) {
        try {
        apiCredentialsRepository.findByClientId(authPair.getClientId())
                .orElseThrow(() -> new EntityNotFoundException("Credentials for this clientId not found"));

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authPair.getClientId(), authPair.getClientSecret()));

        String token = jwtTokenProvider.createToken(authPair.getClientId());

        AuthResponse response = new AuthResponse().setAuthToken(token);

        return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Incorrect clientId or clientSecret");
        }
    }

    @Override
    public InterceptorCredentials getCredentialsForCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal().toString().equals("anonymousUser")) return null;
        Object credentials = authentication.getCredentials();
        if(!(credentials instanceof String)) {
            return null;
        }

        String username = authentication.getName();

        ApiCredentials apiCredentials = apiCredentialsRepository.findByClientId(username)
                .orElseThrow(() -> new EntityNotFoundException("Credentials not found."));
        return new InterceptorCredentials().setClientId(apiCredentials.getClientId())
                .setClientSecret(apiCredentials.getClientSecret())
                .setRefreshToken(apiCredentials.getRefreshToken());
    }
}
