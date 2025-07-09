package io.github.miguelteles.beststickerapp.exception;

import io.github.miguelteles.beststickerapp.exception.enums.StickerDataBaseExceptionEnum;

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
