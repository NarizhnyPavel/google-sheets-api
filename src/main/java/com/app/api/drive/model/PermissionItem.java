package com.app.api.drive.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.validator.routines.EmailValidator;

import javax.validation.ValidationException;

@Data
@Accessors(chain = true)
public class PermissionItem {

    private String id;
    private String type;
    private String role;
    private String emailAddress;

    public void setEmailAddress(String emailAddress) {
        if (!EmailValidator.getInstance().isValid(emailAddress))
            throw new ValidationException("Invalid email address.");
        this.emailAddress = emailAddress;
    }

    @Override
    public String toString() {
        return String.format("{" +
                "  type=\"%s\"" +
                ", role=\"%s\"" +
                ", emailAddress=\"%s\"" +
                '}', type, role, emailAddress);
    }
}
