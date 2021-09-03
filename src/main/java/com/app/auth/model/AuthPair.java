package com.app.auth.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AuthPair {

    private final String clientId;

    private final String clientSecret;

}
