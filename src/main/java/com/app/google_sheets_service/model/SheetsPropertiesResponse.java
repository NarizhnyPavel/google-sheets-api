package com.app.google_sheets_service.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class SheetsPropertiesResponse {
    @JsonProperty("sheets")
    private List<SheetPropertiesEntity> sheets;
    @Data
    public static class SheetPropertiesEntity {
        @JsonProperty("properties")
        private SheetProperties properties;
        @Data
        public static class SheetProperties {
            @JsonProperty("sheetId")
            private Long sheetId;
            @JsonProperty("title")
            private String title;
            @JsonProperty("index")
            private int index;
        }
    }
}
