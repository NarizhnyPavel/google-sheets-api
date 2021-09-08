package com.app.api.drive;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.Map;

@Service
public class GoogleDriveRestServiceImpl implements GoogleDriveRestService {
    private final RestTemplate restTemplate;

    public GoogleDriveRestServiceImpl(@Qualifier("googleDriveRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public void setPermissionsForFile(String fileId, Map<String, String> permissionPair) {

    }

}
