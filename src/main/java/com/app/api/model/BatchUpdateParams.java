package com.app.api.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class BatchUpdateParams {
    private String spreadSheetId;
    private List<String> requests;
}
