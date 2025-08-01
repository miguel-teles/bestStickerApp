package io.github.miguelteles.beststickerapp.exception;

import io.github.miguelteles.beststickerapp.exception.enums.StickerWebCommunicationExceptionEnum;

public class StickerWebCommunicationException extends StickerException {
    private final StickerWebCommunicationExceptionEnum stickerExceptionEnum;

    public StickerWebCommunicationException(Exception ex,
                                            StickerWebCommunicationExceptionEnum stickerFolderExceptionEnum,
                                            String msgError) {
        super(ex, null, msgError);
        this.stickerExceptionEnum = stickerFolderExceptionEnum;
    }

    @Override
    public String getStickerExceptionEnumMessage() {
        return stickerExceptionEnum.toString();
    }
}
