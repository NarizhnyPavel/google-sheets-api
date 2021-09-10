package com.app.api.spreadsheets.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SheetsPropertiesResponse {
    @JsonProperty("sheets")
    private List<SheetPropertiesEntity> sheets;
}
