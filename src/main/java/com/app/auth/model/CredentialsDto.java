package com.app.auth.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CredentialsDto {
    private String clientId;
    private String clientSecret;
    private String refreshToken;
}
