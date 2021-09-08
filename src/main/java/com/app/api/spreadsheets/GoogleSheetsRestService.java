package com.app.api.spreadsheets;

import com.app.api.spreadsheets.model.BatchUpdateParams;
import com.app.api.spreadsheets.model.TableCreationResponse;
import com.app.api.spreadsheets.model.WriteRequestParams;

import java.util.List;

import static com.app.api.spreadsheets.model.SheetsPropertiesResponse.SheetPropertiesEntity;

public interface GoogleSheetsRestService {
    void writeToSpreadSheet(WriteRequestParams writeRequestParams);
    <T> T batchUpdate(BatchUpdateParams batchUpdateParams, Class<T> responseType);
    TableCreationResponse createTable(String tableName);
    List<SheetPropertiesEntity> getSheetsProperties(String spreadsheetId);
}
