package com.app.api;

import com.app.api.model.BatchUpdateParams;
import com.app.api.model.WriteRequestParams;
import com.app.google_sheets_service.model.SheetsPropertiesResponse;
import com.app.google_sheets_service.model.TableCreationResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class GoogleSheetsRestServiceImpl implements GoogleSheetsRestService{

    private final RestTemplate restTemplates;

    private final int MAX_RETRIES_COUNT = 1;
    private final int RETRIES_DELAY = 20000;

    public GoogleSheetsRestServiceImpl(@Qualifier("googleRestTemplate") RestTemplate restTemplates) {
        this.restTemplates = restTemplates;
    }

    @Retryable(maxAttempts=MAX_RETRIES_COUNT, value = {HttpClientErrorException.class, HttpServerErrorException.class},
            backoff = @Backoff(delay = RETRIES_DELAY))
    public void writeToSpreadSheet(WriteRequestParams writeRequestParams){
        restTemplates.put( "/" + writeRequestParams.getSpreadSheetId() + "/values/" + writeRequestParams.getRange() + "?valueInputOption=USER_ENTERED",
                "{    values: " + writeRequestParams.getValues() + "}");
    }

    @Retryable(maxAttempts=MAX_RETRIES_COUNT, value = {HttpClientErrorException.class, HttpServerErrorException.class},
            backoff = @Backoff(delay = RETRIES_DELAY))
    public <T> T batchUpdate(BatchUpdateParams batchUpdateParams, Class<T> responseType){
        return restTemplates.postForObject("/" + batchUpdateParams.getSpreadSheetId() + ":batchUpdate",
                "{  \"requests\": [" +
                        String.join(",", batchUpdateParams.getRequests()) +
                        "   ]}",
                responseType);
    }

    @Retryable(maxAttempts=MAX_RETRIES_COUNT, value = {HttpClientErrorException.class, HttpServerErrorException.class},
            backoff = @Backoff(delay = RETRIES_DELAY))
    public TableCreationResponse createTable(String tableName){
        return restTemplates.postForObject("", "{" +
                        "    \"properties\":{" +
                        "        \"title\": \"" + tableName.replace('\"', ' ') + "\"" +
                        "    }" +
                        "}",
                TableCreationResponse.class);
    }

    @Retryable(maxAttempts=MAX_RETRIES_COUNT, value = {HttpClientErrorException.class, HttpServerErrorException.class},
            backoff = @Backoff(delay = RETRIES_DELAY))
    public SheetsPropertiesResponse getSheetsProperties(String spreadsheetId){
        return restTemplates.getForObject("/" + spreadsheetId + "?&fields=sheets.properties",
                SheetsPropertiesResponse.class);
    }
}
