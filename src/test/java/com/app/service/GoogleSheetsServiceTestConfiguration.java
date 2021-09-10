package com.app.service;

import com.app.api.drive.GoogleDriveRestService;
import com.app.api.spreadsheets.GoogleSheetsRestService;
import com.app.service.domain.GoogleTableRepository;
import com.app.service.domain.GoogleTableSheetRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;

@TestConfiguration
@MockBean(GoogleSheetsRestService.class)
@MockBean(GoogleDriveRestService.class)
@MockBean(GoogleTableSheetRepository.class)
@MockBean(GoogleTableRepository.class)
public class GoogleSheetsServiceTestConfiguration {

    @Bean
    public GoogleSheetsServiceImpl googleSheetsService(GoogleTableRepository googleTableRepository,
                                                       GoogleSheetsRestService googleSheetsRestService,
                                                       GoogleTableSheetRepository googleTableSheetRepository,
                                                       GoogleDriveRestService googleDriveRestService){
        return new GoogleSheetsServiceImpl(googleTableRepository, googleTableSheetRepository,
                googleSheetsRestService, googleDriveRestService);
    }

}

