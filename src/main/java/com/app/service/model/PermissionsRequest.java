package com.app.service.model;

import lombok.Data;

import java.util.List;

@Data
public class PermissionsRequest {
    private List<PermissionsRequestItem> permissions;
}
