package com.app.service;

import com.app.api.drive.GoogleDriveRestService;
import com.app.api.spreadsheets.GoogleSheetsRestService;
import com.app.api.spreadsheets.model.WriteRequestParams;
import com.app.service.domain.GoogleTable;
import com.app.service.domain.GoogleTableRepository;
import com.app.service.domain.GoogleTableSheet;
import com.app.service.domain.GoogleTableSheetRepository;
import com.app.service.model.GoogleSheetsWriteRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.app.utils.ResponseBuilder.buildRequest;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {GoogleSheetsServiceTestConfiguration.class, GoogleSheetsServiceImpl.class})
public class GoogleSheetsServiceTest {

    @Autowired
    private GoogleSheetsService googleSheetsService;

    private final GoogleTableRepository googleTableRepository;
    private final GoogleTableSheetRepository googleTableSheetRepository;
    private final GoogleSheetsRestService sheetsRestService;
    private final GoogleDriveRestService driveRestService;

    @Autowired
    public GoogleSheetsServiceTest(GoogleTableRepository googleTableRepository,
                                   GoogleTableSheetRepository googleTableSheetRepository,
                                   GoogleSheetsRestService sheetsRestService,
                                   GoogleDriveRestService driveRestService) {
        this.sheetsRestService = sheetsRestService;
        this.googleTableRepository = googleTableRepository;
        this.googleTableSheetRepository = googleTableSheetRepository;
        this.driveRestService = driveRestService;
    }

    @Test
    void writeValuesToExistingTable(){
        GoogleSheetsWriteRequest request = buildRequest();
        GoogleTable googleTable = new GoogleTable(1L, randomAlphabetic(10), request.getTableName(), null, Collections.emptyList());
        GoogleTableSheet sheet = new GoogleTableSheet(1L, googleTable, nextInt(1, 10000), request.getSheetName());
        WriteRequestParams writeRequestParams = new WriteRequestParams().setSpreadSheetId(googleTable.getSpreadSheetId())
                .setValues(request.getValues().stream()
                        .map(e -> e.stream()
                                .map(v -> "\"" + (v == null ? "" : v) + "\"")
                                .collect(Collectors.toList())
                                .toString())
                        .collect(Collectors.toList()))
                .setRange("'" + sheet.getSheetName() + "'!B2:C3");

        when(googleTableRepository.findByTableNameAndOwner(request.getTableName(), null))
                .thenReturn(Optional.of(googleTable));
        when(sheetsRestService.getSheetsProperties(googleTable.getSpreadSheetId()))
                .thenReturn(Collections.emptyList());
        when(googleTableSheetRepository.findByTableAndSheetName(googleTable, request.getSheetName()))
                .thenReturn(Optional.of(sheet));

        googleSheetsService.writeToTable(request);
        verify(sheetsRestService).writeToSpreadSheet(writeRequestParams);
    }

}
