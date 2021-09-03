package com.app.api;

import com.app.api.model.BatchUpdateParams;
import com.app.api.model.WriteRequestParams;
import com.app.google_sheets_service.model.SheetsPropertiesResponse;
import com.app.google_sheets_service.model.TableCreationResponse;

public interface GoogleSheetsRestService {
    void writeToSpreadSheet(WriteRequestParams writeRequestParams);
    <T> T batchUpdate(BatchUpdateParams batchUpdateParams, Class<T> responseType);
    TableCreationResponse createTable(String tableName);
    SheetsPropertiesResponse getSheetsProperties(String spreadsheetId);
}
