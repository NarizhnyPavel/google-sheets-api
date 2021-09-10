package com.app.auth;

import com.app.api.CustomOAuthTokenInterceptor;
import com.app.api.GoogleInterceptorFactory;
import com.app.api.InterceptorCredentials;
import com.app.auth.service.AuthService;
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
import java.util.Set;

@Configuration
@Profile("api")
@RequiredArgsConstructor
@Slf4j
public class AuthRestTemplateConfiguration {

    @Primary
    @Bean(name = "restTemplate")
    public RestTemplate defaultRestTemplate() {
        return new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient())){};
    }

    @Autowired
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE,
            proxyMode = ScopedProxyMode.TARGET_CLASS)
    @Bean(name = "googleSheetsRestTemplate")
    public RestTemplate googleSheetsRestTemplate(GoogleInterceptorFactory interceptorFactory,
                                           AuthService authService,
                                           Environment environment) {
        RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient())) {};
        restTemplate.getMessageConverters()
                .add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory(
                "https://sheets.googleapis.com/v4/spreadsheets");
        restTemplate.setUriTemplateHandler(uriBuilderFactory);
        setIInterceptor(interceptorFactory, authService, environment, restTemplate);
        return restTemplate;
    }

    @Autowired
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE,
            proxyMode = ScopedProxyMode.TARGET_CLASS)
    @Bean(name = "googleDriveRestTemplate")
    public RestTemplate googleDriveRestTemplate(GoogleInterceptorFactory interceptorFactory,
                                           AuthService authService,
                                           Environment environment) {
        RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient())) {};
        restTemplate.getMessageConverters()
                .add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory(
                "https://www.googleapis.com/drive/v3/files");
        restTemplate.setUriTemplateHandler(uriBuilderFactory);
        setIInterceptor(interceptorFactory, authService, environment, restTemplate);
        return restTemplate;
    }

    private void setIInterceptor(GoogleInterceptorFactory interceptorFactory, AuthService authService, Environment environment, RestTemplate restTemplate) {
        String refreshToken = environment.getProperty("google-api.refreshToken");
        String authCode = environment.getProperty("google-api.authCode");
        if (authCode != null && !authCode.isEmpty() || refreshToken != null && !refreshToken.isEmpty()) {
            InterceptorCredentials interceptorCredentials = new InterceptorCredentials()
                    .setRefreshToken(refreshToken)
                    .setAuthCode(authCode);
            CustomOAuthTokenInterceptor interceptor = interceptorFactory.getInterceptor(interceptorCredentials);
            restTemplate.setInterceptors(Collections.singletonList(interceptor));
        } else {
            CustomOAuthTokenInterceptor interceptor = interceptorFactory.getInterceptor(authService.getCredentialsForCurrentUser());
            if (interceptor != null)
                restTemplate.setInterceptors(Collections.singletonList(interceptor));
            else throw new RuntimeException("Refresh token or AuthCode for google-api required!");
        }
    }

    @Bean
    public CloseableHttpClient httpClient() {
        return HttpClientBuilder
                .create()
                .build();
    }

}
