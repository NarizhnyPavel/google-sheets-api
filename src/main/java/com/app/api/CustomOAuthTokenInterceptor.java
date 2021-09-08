package com.app.api;

import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Slf4j
public class CustomOAuthTokenInterceptor implements ClientHttpRequestInterceptor {
    private final RestTemplate restTemplate;
    private final InterceptorCredentials credentials;
    private OAuth2AccessToken tokenCache;
    private final Environment environment;

    public CustomOAuthTokenInterceptor(Environment environment,
                                       RestTemplate restTemplate,
                                       InterceptorCredentials credentials) {
        this.restTemplate = restTemplate;
        this.environment = environment;
        this.credentials = credentials;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        request.getHeaders().add("Authorization",
                "Bearer " + getAccessToken().getValue());
        return execution.execute(request, body);
    }

    public OAuth2AccessToken getAccessToken() {
        String clientId = environment.getProperty("google-api.clientId");
        String clientSecret = environment.getProperty("google-api.clientSecret");
        if ((clientId == null || clientId.isEmpty()) && (clientSecret == null || clientSecret.isEmpty()))
            throw new RuntimeException("Credentials for google-api required!");
        if (tokenCache == null || this.tokenCache.isExpired()) {
            log.debug("Fetching new token");
            if (credentials.getRefreshToken() == null || credentials.getRefreshToken().isEmpty())
                getRefreshToken(credentials.getAuthCode(), clientId, clientSecret);
            else this.tokenCache = restTemplate.postForObject(
                    "https://oauth2.googleapis.com/token?" +
                            "client_id=" + clientId +
                            "&client_secret=" + clientSecret +
                            "&refresh_token=" + credentials.getRefreshToken() +
                            "&grant_type=refresh_token",
                    null,
                    OAuth2AccessToken.class);
        } else
            log.debug("Token still valid for {} seconds", this.tokenCache.getExpiresIn());
        return this.tokenCache;
    }

    private void getRefreshToken(String code, String clientId, String clientSecret){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type","authorization_code");
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("code", code);
        map.add("redirect_uri", "urn:ietf:wg:oauth:2.0:oob");

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

        try {
            ResponseEntity<OAuth2AccessToken> token =
                    restTemplate.exchange("https://oauth2.googleapis.com/token",
                            HttpMethod.POST,
                            entity,
                            OAuth2AccessToken.class);
            if (token.getBody() != null)
                this.credentials.setRefreshToken(token.getBody().getRefreshToken().getValue());
            this.tokenCache = token.getBody();
        } catch (RestClientException e){
            log.warn("getRefreshToken(): Invalid authCode for OAuth2 authorization. Refresh code!");
            throw new IllegalStateException("Invalid authCode for OAuth2 authorization. Refresh code!");
        }
    }

}
