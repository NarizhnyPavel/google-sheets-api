package com.app.google_sheets_service.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TableCreationResponse {

    @JsonProperty("spreadsheetId")
    private String spreadSheetId;
}
