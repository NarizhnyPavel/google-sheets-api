package com.app.api;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Set;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class InterceptorCredentials {
    private String refreshToken;
    private String authCode;
}
