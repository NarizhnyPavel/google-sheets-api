package com.app.api.drive;

import com.app.api.drive.model.PermissionItem;
import com.app.api.drive.model.PermissionResponse;
import com.app.api.drive.model.PermissionRole;
import com.app.service.model.PermissionsRequestItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GoogleDriveRestServiceImpl implements GoogleDriveRestService {
    private final RestTemplate restTemplate;

    @Value("${retries.max-count: 5}")
    private final int MAX_RETRIES_COUNT = 5;
    @Value("${retries.delay: 20000}")
    private final int RETRIES_DELAY = 20000;
    private HttpHeaders headers;

    public GoogleDriveRestServiceImpl(@Qualifier("googleDriveRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.headers = new HttpHeaders();
        this.headers.setContentType(MediaType.APPLICATION_JSON);
    }

    @Override
    public void setPermissionsForFile(String fileId, boolean replace, List<PermissionsRequestItem> requiredPermissions) {
        List<PermissionItem> existingPermissions = getPermissions(fileId);
        requiredPermissions.forEach(p -> {
            PermissionItem item = new PermissionItem()
                    .setRole(PermissionRole.getByName(p.getRole()).getName());
            if (p.getUser().contains("@"))
                item.setType("user").setEmailAddress(p.getUser());
            else
                item.setType("anyone");
            existingPermissions.stream()
                    .filter(i -> i.getType().equals(item.getType())
                            && (i.getEmailAddress() == null || i.getEmailAddress().equals(item.getEmailAddress())))
                    .findFirst()
                    .ifPresentOrElse(ex -> {
                        if (!ex.getRole().equalsIgnoreCase(item.getRole())) updatePermission(fileId, ex.getId(), item);
                        existingPermissions.remove(ex);
                    }, () -> createPermission(fileId, item));
        });
        if (replace) existingPermissions.forEach(p -> deletePermission(fileId, p));
    }

    @Retryable(maxAttempts=MAX_RETRIES_COUNT, value = {HttpClientErrorException.class, HttpServerErrorException.class},
            backoff = @Backoff(delay = RETRIES_DELAY))
    public void createPermission(String fileId, PermissionItem item){
        String url = String.format("/%s/permissions", fileId);
        String request = String.format("{" +
                "\"role\": \"%s\"," +
                "\"type\": \"%s\"" +
                "}", item.getRole(), item.getType());
        if (item.getEmailAddress() != null)
            request = request.replace("}", ",\"emailAddress\": \"" + item.getEmailAddress() + "\"}");
        HttpEntity<String> entity = new HttpEntity<>(request, headers);
        restTemplate.postForObject(url, entity, Object.class);
    }

    @Retryable(maxAttempts=MAX_RETRIES_COUNT, value = {HttpClientErrorException.class, HttpServerErrorException.class},
            backoff = @Backoff(delay = RETRIES_DELAY))
    public void updatePermission(String fileId, String permissionId, PermissionItem item){
        String url = String.format("/%s/permissions/%s", fileId, permissionId);
        if (item.getEmailAddress() != null && item.getEmailAddress().endsWith("@gmail.com"))
            url += "?sendNotificationEmail=false";
        String request = String.format("{" +
                "\"role\": \"%s\"" +
                "}", item.getRole());
        HttpEntity<String> entity = new HttpEntity<>(request, headers);
        restTemplate.patchForObject(url, entity, Object.class);
    }

    @Retryable(maxAttempts=MAX_RETRIES_COUNT, value = {HttpClientErrorException.class, HttpServerErrorException.class},
            backoff = @Backoff(delay = RETRIES_DELAY))
    public void deletePermission(String fileId, PermissionItem item){
        if (item.getId() == null) return;
        String url = String.format("/%s/permissions/%s", fileId, item.getId());
        restTemplate.delete(url, Object.class);
    }

    @Retryable(maxAttempts=MAX_RETRIES_COUNT, value = {HttpClientErrorException.class, HttpServerErrorException.class},
            backoff = @Backoff(delay = RETRIES_DELAY))
    public List<PermissionItem> getPermissions(String fileId){
        String url = String.format("/%s/permissions?fields=permissions(kind,id,type,emailAddress,role,deleted)", fileId);
        return Optional.ofNullable(restTemplate.getForObject(url, PermissionResponse.class))
                .orElse(new PermissionResponse())
                .getPermissions().stream()
                .filter(i -> !i.getRole().equalsIgnoreCase("owner"))
                .collect(Collectors.toList());
    }

}
