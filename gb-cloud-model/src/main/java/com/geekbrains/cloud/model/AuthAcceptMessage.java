package com.geekbrains.cloud.model;

import lombok.Data;

@Data
public class AuthAcceptMessage implements CloudMessage{
    private boolean isAuthenticated;
    private String serverResponse;

    public AuthAcceptMessage(boolean isAuthenticated, String serverResponse) {
        this.isAuthenticated = isAuthenticated;
        this.serverResponse = serverResponse;
    }
}
