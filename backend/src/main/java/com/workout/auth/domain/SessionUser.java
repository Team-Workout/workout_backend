package com.workout.auth.domain;

import java.io.Serializable;

public class SessionUser implements Serializable {
    private final String email;
    private final String name;

    public SessionUser(String email, String name) {
        this.email = email;
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }
}