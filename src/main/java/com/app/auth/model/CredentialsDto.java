package com.app.auth.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CredentialsDto {
    private String login;
    private String authCode;
}
