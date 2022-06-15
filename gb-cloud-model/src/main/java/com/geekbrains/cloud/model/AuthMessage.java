package com.geekbrains.cloud.model;

import lombok.Data;

@Data
public class AuthMessage implements CloudMessage{
    private String userName;
    private String password;
    private boolean isNewUser;


    public AuthMessage(String userName, String password, boolean isNewUser) {
        this.userName = userName;
        this.password = password;
        this.isNewUser = isNewUser;
    }
}
