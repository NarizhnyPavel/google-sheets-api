package com.app.api.spreadsheets.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class ListCreationResponse {
    private int sheetId;

    @JsonProperty("replies")
    private void unpackNested(Map replies) {
        Map addSheet = (Map)replies.get("addSheet");
        Map properties = (Map)addSheet.get("properties");
        this.sheetId = (int) properties.get("sheetId");
    }

    public int getSheetId(){
        return this.sheetId;
    }

}
