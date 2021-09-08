package com.app.api.spreadsheets.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class WriteRequestParams {
    private String spreadSheetId;
    private String range;
    private List<String> values;
}
