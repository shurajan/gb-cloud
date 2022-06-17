package com.geekbrains.cloud.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class FileSystemObject implements Serializable {
    private String name;
    private boolean isFolder;

    public FileSystemObject(String name, boolean isFolder) {
        this.name = name;
        this.isFolder = isFolder;
    }
}
