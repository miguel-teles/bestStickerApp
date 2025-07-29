package io.github.miguelteles.beststickerapp.exception;

import io.github.miguelteles.beststickerapp.exception.enums.StickerDataBaseExceptionEnum;
import io.github.miguelteles.beststickerapp.exception.enums.StickerFatalExceptionEnum;

public class StickerFatalErrorException extends StickerException {

    private final StickerFatalExceptionEnum stickerExceptionEnum;
    public StickerFatalErrorException(Exception ex,
                                      StickerFatalExceptionEnum stickerExceptionEnum,
                                    String msgError) {
        super(ex, null, msgError);
        this.stickerExceptionEnum = stickerExceptionEnum;
    }

    @Override
    public String getStickerExceptionEnumMessage() {
        return stickerExceptionEnum.toString();
    }

}
