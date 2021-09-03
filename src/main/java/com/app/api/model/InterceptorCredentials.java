package com.app.api.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class InterceptorCredentials {
    private String clientId;
    private String clientSecret;
    private String refreshToken;
}
