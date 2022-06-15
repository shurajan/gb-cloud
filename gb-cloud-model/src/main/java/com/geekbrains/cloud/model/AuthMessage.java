package com.geekbrains.cloud.model;

import lombok.Data;

@Data
public class AuthMessage implements CloudMessage{
    private String userName;
    private String password;


    public AuthMessage(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }
}
