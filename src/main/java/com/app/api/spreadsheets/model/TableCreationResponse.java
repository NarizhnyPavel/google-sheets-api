package com.app.api.spreadsheets.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TableCreationResponse {

    @JsonProperty("spreadsheetId")
    private String spreadSheetId;
}
