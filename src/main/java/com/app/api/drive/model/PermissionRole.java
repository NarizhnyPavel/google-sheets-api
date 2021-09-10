package com.app.api.drive.model;

import javax.validation.ValidationException;
import java.util.Arrays;

public enum PermissionRole {
    OWNER("owner"),
    WRITER("writer"),
    READER("reader"),
    COMMENTER("commenter");

    private final String name;

    PermissionRole(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static PermissionRole getByName(String name){
        return Arrays.stream(PermissionRole.values())
                .filter(v -> v.name.equalsIgnoreCase(name))
                .findFirst().orElseThrow(() -> new ValidationException(String.format("Invalid role name (%s).", name)));
    }

}
