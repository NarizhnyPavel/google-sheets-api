package com.app.config;

import com.app.api.CustomOAuthTokenInterceptor;
import com.app.api.GoogleInterceptorFactory;
import com.app.api.InterceptorCredentials;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
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
    @Bean(name = "googleSheetsRestTemplate")
    public RestTemplate googleSheetsRestTemplate(GoogleInterceptorFactory interceptorFactory,
                                                Environment environment) {
        RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient())) {};
        restTemplate.getMessageConverters()
                .add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory(
                "https://sheets.googleapis.com/v4/spreadsheets");
        restTemplate.setUriTemplateHandler(uriBuilderFactory);
        setInterceptor(interceptorFactory, environment, restTemplate);
        return restTemplate;
    }

    @Autowired
    @Bean(name = "googleDriveRestTemplate")
    public RestTemplate googleDriveSheetsRestTemplate(GoogleInterceptorFactory interceptorFactory,
                                                 Environment environment) {
        RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient())) {};
        restTemplate.getMessageConverters()
                .add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory(
                "https://www.googleapis.com/drive/v3/files");
        restTemplate.setUriTemplateHandler(uriBuilderFactory);
        setInterceptor(interceptorFactory, environment, restTemplate);
        return restTemplate;
    }

    private void setInterceptor(GoogleInterceptorFactory interceptorFactory, Environment environment, RestTemplate restTemplate) {
        String refreshToken = environment.getProperty("google-api.refreshToken");
        String authCode = environment.getProperty("google-api.authCode");
        if (refreshToken != null && !refreshToken.isEmpty() || authCode != null && !authCode.isEmpty()) {
            CustomOAuthTokenInterceptor interceptor = interceptorFactory.getInterceptor(
                    new InterceptorCredentials()
                            .setRefreshToken(refreshToken)
                            .setAuthCode(authCode));
            restTemplate.setInterceptors(Collections.singletonList(interceptor));
        }
        else throw new RuntimeException("Credentials for google-api required!");
    }

    @Bean
    public CloseableHttpClient httpClient() {
        return HttpClientBuilder
                .create()
                .setDefaultHeaders(Collections.singletonList(new BasicHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)))
                .build();
    }

}
