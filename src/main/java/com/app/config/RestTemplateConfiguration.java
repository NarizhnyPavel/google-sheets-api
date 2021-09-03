package com.app.config;

import com.app.api.CustomOAuthTokenInterceptor;
import com.app.api.GoogleInterceptorFactory;
import com.app.api.model.InterceptorCredentials;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

@Profile({"dev", "prom"})
@Configuration
@RequiredArgsConstructor
@Slf4j
public class RestTemplateConfiguration {

    @Primary
    @Bean(name = "restTemplate")
    public RestTemplate defaultRestTemplate() {
        return new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient())){};
    }

    @Autowired
    @Bean(name = "googleRestTemplate")
    public RestTemplate googleRestTemplate(GoogleInterceptorFactory interceptorFactory,
                                           Environment environment) {
        RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient())) {};
        DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory(
                "https://sheets.googleapis.com/v4/spreadsheets");
        restTemplate.setUriTemplateHandler(uriBuilderFactory);
        restTemplate.getMessageConverters()
                .add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        String clientId = environment.getProperty("google-api.clientId");
        String clientSecret = environment.getProperty("google-api.clientSecret");
        String refreshToken = environment.getProperty("google-api.refreshToken");
        if (clientId != null && !clientId.isEmpty() && clientSecret != null && !clientSecret.isEmpty()
                && refreshToken != null && !refreshToken.isEmpty()) {
            CustomOAuthTokenInterceptor interceptor = interceptorFactory.getInterceptor(
                    new InterceptorCredentials()
                            .setClientId(clientId)
                            .setClientSecret(clientSecret)
                            .setRefreshToken(refreshToken));
            OAuth2AccessToken token = interceptor.getRefreshedToken(new InterceptorCredentials()
                    .setClientId(clientId)
                    .setClientSecret(clientSecret)
                    .setRefreshToken(refreshToken));
            if (!token.getScope().contains("https://www.googleapis.com/auth/spreadsheets"))
                throw new BadCredentialsException("Credentials scopes must contain \"https://www.googleapis.com/auth/spreadsheets\"");
            restTemplate.setInterceptors(Collections.singletonList(interceptor));
        } else throw new RuntimeException("Credentials for google-api required!");
        return restTemplate;
    }

    @Bean
    public CloseableHttpClient httpClient() {
        return HttpClientBuilder
                .create()
                .build();
    }

}
