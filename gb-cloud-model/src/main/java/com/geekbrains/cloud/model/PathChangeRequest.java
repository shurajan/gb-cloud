package com.geekbrains.cloud.model;

import lombok.Data;

@Data
public class PathChangeRequest implements CloudMessage{

    private final String newFolder;

}
