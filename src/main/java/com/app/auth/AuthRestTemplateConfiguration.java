package com.app.auth;

import com.app.api.CustomOAuthTokenInterceptor;
import com.app.api.GoogleInterceptorFactory;
import com.app.api.model.InterceptorCredentials;
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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

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
    @Bean(name = "googleRestTemplate")
    public RestTemplate googleRestTemplate(GoogleInterceptorFactory interceptorFactory,
                                           AuthService authService,
                                           Environment environment) {
        RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient())) {};
        DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory(
                "https://sheets.googleapis.com/v4/spreadsheets"
        );
        restTemplate.setUriTemplateHandler(uriBuilderFactory);
        restTemplate.getMessageConverters()
                .add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        if (environment.getProperty("google-api.clientId") != null &&
                environment.getProperty("google-api.clientSecret") != null &&
                environment.getProperty("google-api.refreshToken") != null)
            restTemplate.setInterceptors(Collections.singletonList(interceptorFactory.getInterceptor(new InterceptorCredentials()
                    .setClientId(environment.getProperty("google-api.clientId"))
                    .setClientSecret(environment.getProperty("google-api.clientSecret"))
                    .setRefreshToken(environment.getProperty("google-api.refreshToken")))));
        else {
            CustomOAuthTokenInterceptor interceptor = interceptorFactory.getInterceptor(authService.getCredentialsForCurrentUser());
            if (interceptor != null)
                restTemplate.setInterceptors(Collections.singletonList(interceptor));
        }
        return restTemplate;
    }

    @Bean
    public CloseableHttpClient httpClient() {
        return HttpClientBuilder
                .create()
                .build();
    }

}
