package com.app.service.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.commons.validator.routines.EmailValidator;

import javax.validation.ValidationException;

@Data
public class PermissionsRequestItem {
    private String role;
    private String user;

    @JsonProperty("user")
    public void setUser(String user){
        if (user.contains("@")){
            if (!EmailValidator.getInstance().isValid(user))
                throw new ValidationException("Invalid email address.");
            this.user = user;
        }
        this.user = user;
    }

    public String getUser() {
        return user != null ? user : "";
    }
}
