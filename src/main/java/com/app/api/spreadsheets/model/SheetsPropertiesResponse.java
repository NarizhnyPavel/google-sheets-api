package com.app.api.spreadsheets.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SheetsPropertiesResponse {
    @JsonProperty("sheets")
    private List<SheetPropertiesEntity> sheets;
    @Data
    public static class SheetPropertiesEntity {
        @JsonProperty("sheetId")
        private int sheetId;
        @JsonProperty("title")
        private String title;
        @JsonProperty("index")
        private int index;

        @JsonProperty("properties")
        private void unpackNested(Map props) {
            this.sheetId = (int) props.get("sheetId");
            this.title = (String) props.get("title");
            this.index = (int) props.get("index");
        }
    }
}
