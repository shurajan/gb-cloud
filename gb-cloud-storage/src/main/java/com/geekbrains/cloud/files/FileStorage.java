package com.geekbrains.cloud.files;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class FileStorage {
    public List<String> getFiles(String dir) {
        String[] list = new File(dir).list();
        assert list != null;
        return Arrays.asList(list);
    }
}
