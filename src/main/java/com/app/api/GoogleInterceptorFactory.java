package com.app.api;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public final class GoogleInterceptorFactory {
    private final RestTemplate restTemplate;
    private final Environment environment;
    private final HashMap<String, CustomOAuthTokenInterceptor> interceptors = new HashMap<>();

    public CustomOAuthTokenInterceptor getInterceptor(InterceptorCredentials credentials) {
        if (credentials == null) return null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String clientId = null;
        if (authentication != null && authentication.getPrincipal() != null)
            clientId = authentication.getPrincipal().toString();
        if (interceptors.get(clientId) == null)
            interceptors.put(clientId, new CustomOAuthTokenInterceptor(environment, restTemplate, credentials));
        return interceptors.get(clientId);
    }
}
