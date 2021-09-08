package com.app.auth.service;

import com.app.api.CustomOAuthTokenInterceptor;
import com.app.api.GoogleInterceptorFactory;
import com.app.api.InterceptorCredentials;
import com.app.auth.domain.ApiCredentials;
import com.app.auth.domain.ApiCredentialsRepository;
import com.app.auth.model.AuthRequest;
import com.app.auth.model.AuthResponse;
import com.app.auth.model.TokenResponse;
import com.app.auth.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;

@Service

@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{

    private final ApiCredentialsRepository apiCredentialsRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final GoogleInterceptorFactory googleInterceptorFactory;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public ResponseEntity<AuthResponse> authenticate(AuthRequest authRequest) {
        String login = String.valueOf(authRequest.getAuthCode().hashCode());
        ApiCredentials apiCredentials = apiCredentialsRepository.findByLogin(login)
                .orElseGet(() -> {
                    InterceptorCredentials interceptorCredentials = new InterceptorCredentials().setAuthCode(authRequest.getAuthCode());
                    CustomOAuthTokenInterceptor interceptor = googleInterceptorFactory.getInterceptor(interceptorCredentials);
                    OAuth2AccessToken token = interceptor.getAccessToken();
                    return new ApiCredentials()
                            .setLogin(login)
                            .setPassword(passwordEncoder.encode(authRequest.getAuthCode()))
                            .setRefreshToken(token.getRefreshToken().getValue());
                });
        apiCredentialsRepository.save(apiCredentials);

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(login, authRequest.getAuthCode()));

        String token = jwtTokenProvider.createToken(login);

        return ResponseEntity.ok(new AuthResponse().setAuthToken(token));
    }

    @Override
    public ResponseEntity<TokenResponse> getRefreshToken(AuthRequest authRequest) {
        String login = String.valueOf(authRequest.getAuthCode().hashCode());
        ApiCredentials apiCredentials = apiCredentialsRepository.findByLogin(login)
                .orElseGet(() -> {
                    InterceptorCredentials interceptorCredentials = new InterceptorCredentials().setAuthCode(authRequest.getAuthCode());
                    CustomOAuthTokenInterceptor interceptor = googleInterceptorFactory.getInterceptor(interceptorCredentials);
                    OAuth2AccessToken token = interceptor.getAccessToken();
                    return new ApiCredentials().setRefreshToken(token.getRefreshToken().getValue());
                });

        return ResponseEntity.ok(new TokenResponse().setRefreshToken(apiCredentials.getRefreshToken()));
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

        ApiCredentials apiCredentials = apiCredentialsRepository.findByLogin(username)
                .orElseThrow(() -> new EntityNotFoundException("Credentials not found."));
        return new InterceptorCredentials()
                .setRefreshToken(apiCredentials.getRefreshToken());
    }
}
