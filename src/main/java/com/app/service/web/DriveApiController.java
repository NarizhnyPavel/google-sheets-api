package com.app.service.web;

import com.app.api.drive.GoogleDriveRestService;
import com.app.service.model.PermissionsRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/drive")
@RequiredArgsConstructor
public class DriveApiController {

    private final GoogleDriveRestService googleDriveRestService;

    @PatchMapping("/{fileId}")
    public void updatePermissions(@PathVariable String fileId, @RequestBody PermissionsRequest request){
        googleDriveRestService.setPermissionsForFile(fileId, false, request.getPermissions());
    }

    @PostMapping("/{fileId}")
    public void setPermissions(@PathVariable String fileId, @RequestBody PermissionsRequest request){
        googleDriveRestService.setPermissionsForFile(fileId, true, request.getPermissions());
    }

}
