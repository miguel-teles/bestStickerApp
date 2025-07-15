package io.github.miguelteles.beststickerapp.exception;

import io.github.miguelteles.beststickerapp.exception.enums.StickerHttpClientExceptionEnum;

public class StickerHttpClientException extends StickerException {
    private final StickerHttpClientExceptionEnum stickerExceptionEnum;

    public StickerHttpClientException(Exception ex,
                                      StickerHttpClientExceptionEnum stickerFolderExceptionEnum,
                                      String msgError) {
        super(ex, null, msgError);
        this.stickerExceptionEnum = stickerFolderExceptionEnum;
    }

    @Override
    public String getStickerExceptionEnumMessage() {
        return stickerExceptionEnum.toString();
    }
}
