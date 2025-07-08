package com.example.samplestickerapp.exception;

import com.example.samplestickerapp.exception.enums.StickerFolderExceptionEnum;

public class StickerFolderException extends StickerException {
    private final StickerFolderExceptionEnum stickerExceptionEnum;
    public StickerFolderException(Exception ex,
                            StickerFolderExceptionEnum stickerFolderExceptionEnum,
                            String msgError) {
        super(ex, null, msgError);
        this.stickerExceptionEnum = stickerFolderExceptionEnum;
    }

    @Override
    public String getStickerExceptionEnumMessage() {
        return stickerExceptionEnum.toString();
    }
}
