package com.geekbrains.cloud.model;

import lombok.Data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ListFiles implements CloudMessage {

    private final List<String> files;
    private final List<FileSystemObject> folderContent;

    public ListFiles(Path path) throws IOException {
        files = Files.list(path)
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toList());
        folderContent = Files.list(path)
                .map(p -> {return new FileSystemObject(p.getFileName().toString(), Files.isDirectory(p));}
                )
                .collect(Collectors.toList());
    }

}
