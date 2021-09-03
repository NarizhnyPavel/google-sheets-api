package com.app.api;

import com.app.api.model.InterceptorCredentials;
import com.sun.istack.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Service
@Data
@Accessors(chain = true)
@Slf4j
public class CustomOAuthTokenInterceptor implements ClientHttpRequestInterceptor {
    private final RestTemplate restTemplate;
    private InterceptorCredentials credentials;
    private OAuth2AccessToken tokenCache;

    @NotNull
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        request.getHeaders().add("Authorization",
                "Bearer " + getRefreshedToken(credentials).getValue());
        return execution.execute(request, body);
    }

    public OAuth2AccessToken getRefreshedToken(InterceptorCredentials interceptorCredentials) {
        if (tokenCache == null || tokenCache.isExpired()) {
            log.debug("Fetching new token");
            tokenCache = restTemplate.postForObject(
                    "https://oauth2.googleapis.com/token?" +
                            "client_id=" + interceptorCredentials.getClientId() +
                            "&client_secret=" + interceptorCredentials.getClientSecret() +
                            "&refresh_token=" + interceptorCredentials.getRefreshToken() +
                            "&grant_type=refresh_token",
                    null,
                    OAuth2AccessToken.class);
        } else
            log.debug("Token still valid for {} seconds", tokenCache.getExpiresIn());
         return tokenCache;
    }
}
