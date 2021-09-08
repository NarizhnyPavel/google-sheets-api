package com.app.api.drive;

import java.util.Map;

public interface GoogleDriveRestService {

    /**
     * @param permissionPair {email} - {role: [owner, writer, commenter, reader]}
     */
    void setPermissionsForFile(String fileId, Map<String, String> permissionPair);

}
