package io.github.miguelteles.beststickerapp.domain.pojo;

public record GeneratePresignedURLUploadVideoRQ(String fileName, OperationType operation) {

    public enum OperationType {
        UPLOAD,
        DOWNLOAD;
    }

}