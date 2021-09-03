package com.app;

import com.app.google_sheets_service.service.GoogleSheetsService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@EnableRetry
@RequiredArgsConstructor
public class GoogleSheetsApiApplication {

    private final GoogleSheetsService googleSheetsService;

    public static void main(String[] args) {
        SpringApplication.run(GoogleSheetsApiApplication.class, args);
    }

}
