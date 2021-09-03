package com.app.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ListCreationResponse {
    @JsonProperty("replies")
    private List<AddSheetObject> sheetObjects;

    public Long getSheetId(){
        if (sheetObjects == null || sheetObjects.size() == 0) return null;
        return sheetObjects.get(0)
                .getAddSheet().getAddSheetProperties().getSheetId();
    }

    @Data
    public static class AddSheetObject {
        @JsonProperty("addSheet")
        private AddSheet addSheet;

        @Data
        public static class AddSheet {
            @JsonProperty("properties")
            AddSheetProperties addSheetProperties;

            @Data
            public static class AddSheetProperties {
                @JsonProperty("sheetId")
                private Long sheetId;
            }
        }
    }

}
