package io.github.miguelteles.beststickerapp.domain.pojo;

import java.io.Serializable;

public class GeneratePresignedURLUploadVideoRQ {

    private final String fileName;
    private final String operation;

    public GeneratePresignedURLUploadVideoRQ(String fileName, OperationType operation) {
        this.fileName = fileName;
        this.operation = operation.getValor();
    }

    public String getFileName() {
        return fileName;
    }

    public String getOperation() {
        return operation;
    }

    public enum OperationType implements Serializable {
        UPLOAD("upload"),
        DOWNLOAD("download");

        private final String valor;

        OperationType(String valor) {
            this.valor = valor;
        }

        public String getValor() {
            return valor;
        }
    }

}