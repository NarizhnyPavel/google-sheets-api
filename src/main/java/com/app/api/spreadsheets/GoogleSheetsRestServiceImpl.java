package com.app.api.spreadsheets;

import com.app.api.spreadsheets.model.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class GoogleSheetsRestServiceImpl implements GoogleSheetsRestService{

    private final RestTemplate restTemplate;

    @Value("${retries.max-count: 5}")
    private final int MAX_RETRIES_COUNT = 5;
    @Value("${retries.delay: 20000}")
    private final int RETRIES_DELAY = 20000;

    public GoogleSheetsRestServiceImpl(@Qualifier("googleSheetsRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Retryable(maxAttempts=MAX_RETRIES_COUNT, value = {HttpClientErrorException.class, HttpServerErrorException.class},
            backoff = @Backoff(delay = RETRIES_DELAY))
    public void writeToSpreadSheet(WriteRequestParams writeRequestParams){
        restTemplate.put( "/" + writeRequestParams.getSpreadSheetId() + "/values/" + writeRequestParams.getRange() + "?valueInputOption=USER_ENTERED",
                "{    values: " + writeRequestParams.getValues() + "}");
    }

    @Retryable(maxAttempts=MAX_RETRIES_COUNT, value = {HttpClientErrorException.class, HttpServerErrorException.class},
            backoff = @Backoff(delay = RETRIES_DELAY))
    public <T> T batchUpdate(BatchUpdateParams batchUpdateParams, Class<T> responseType){
        return restTemplate.postForObject("/" + batchUpdateParams.getSpreadSheetId() + ":batchUpdate",
                "{  \"requests\": [" +
                        String.join(",", batchUpdateParams.getRequests()) +
                        "   ]}",
                responseType);
    }

    @Retryable(maxAttempts=MAX_RETRIES_COUNT, value = {HttpClientErrorException.class, HttpServerErrorException.class},
            backoff = @Backoff(delay = RETRIES_DELAY))
    public TableCreationResponse createTable(String tableName){
        return restTemplate.postForObject("", "{" +
                        "    \"properties\":{" +
                        "        \"title\": \"" + tableName.replace('\"', ' ') + "\"" +
                        "    }" +
                        "}",
                TableCreationResponse.class);
    }

    @Retryable(maxAttempts=MAX_RETRIES_COUNT, value = {HttpClientErrorException.class, HttpServerErrorException.class},
            backoff = @Backoff(delay = RETRIES_DELAY))
    public List<SheetPropertiesEntity> getSheetsProperties(String spreadsheetId){
        return Optional.ofNullable(restTemplate.getForObject("/" + spreadsheetId + "?&fields=sheets.properties",
                SheetsPropertiesResponse.class)).orElse(new SheetsPropertiesResponse()).getSheets();
    }
}
