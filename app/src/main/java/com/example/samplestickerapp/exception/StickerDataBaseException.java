package com.example.samplestickerapp.exception;

import com.example.samplestickerapp.exception.enums.StickerDataBaseExceptionEnum;

public class StickerDataBaseException extends StickerException {

    private final StickerDataBaseExceptionEnum stickerExceptionEnum;
    public StickerDataBaseException(Exception ex,
                                    StickerDataBaseExceptionEnum stickerDataBaseExceptionEnum,
                                    String msgError) {
        super(ex, null, msgError);
        this.stickerExceptionEnum = stickerDataBaseExceptionEnum;
    }

    @Override
    public String getStickerExceptionEnumMessage() {
        return stickerExceptionEnum.toString();
    }
}
