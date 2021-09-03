package com.app.api;

import com.app.api.model.InterceptorCredentials;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

@Component
@RequiredArgsConstructor
public final class GoogleInterceptorFactory {
    private final RestTemplate restTemplate;
    private final HashMap<String, CustomOAuthTokenInterceptor> interceptors = new HashMap<>();

    public CustomOAuthTokenInterceptor getInterceptor(InterceptorCredentials credentialsDto) {
        if (credentialsDto == null) return null;
        if (interceptors.get(credentialsDto.getClientId()) == null)
            interceptors.put(credentialsDto.getClientId(),
                    new CustomOAuthTokenInterceptor(restTemplate).setCredentials(new InterceptorCredentials()
                            .setClientId(credentialsDto.getClientId())
                            .setClientSecret(credentialsDto.getClientSecret())
                            .setRefreshToken(credentialsDto.getRefreshToken())));
        return interceptors.get(credentialsDto.getClientId());
    }
}
