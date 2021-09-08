package com.app.service.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class WriteResponse {
    private String tableUrl;
}
