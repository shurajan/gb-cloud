package com.geekbrains.cloud.application.ui;

import com.geekbrains.cloud.model.FileSystemObject;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


public class FileSystemObjectCell extends ListCell<FileSystemObject> {
    private final Image folderImage;

    public FileSystemObjectCell(Image folderImage) {
        this.folderImage = folderImage;
    }

    @Override
    public void updateItem(FileSystemObject name, boolean empty) {
        super.updateItem(name, empty);
        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            setText(name.getName());
            if(name.isFolder()) {
                ImageView imageView = new ImageView();
                imageView.setImage(folderImage);
                imageView.setFitHeight(15);
                imageView.setFitWidth(23);
                setGraphic(imageView);
            }
        }
    }
}

