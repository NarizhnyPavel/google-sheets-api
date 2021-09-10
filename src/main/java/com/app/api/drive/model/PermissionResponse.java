package com.app.api.drive.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class PermissionResponse {
    @JsonProperty("permissions")
    private List<PermissionItem> permissions;
}
