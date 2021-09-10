package com.app.api.drive;

import com.app.service.model.PermissionsRequestItem;

import java.util.List;
import java.util.Map;

public interface GoogleDriveRestService {

    /**
     * @param replace заменить права (Y) / обновить для имеющихся (N)
     * @param requiredPermissions {@link PermissionsRequestItem}
     */
    void setPermissionsForFile(String fileId, boolean replace, List<PermissionsRequestItem> requiredPermissions);

}
