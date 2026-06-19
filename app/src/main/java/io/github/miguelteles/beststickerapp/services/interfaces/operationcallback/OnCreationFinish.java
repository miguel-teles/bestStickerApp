package io.github.miguelteles.beststickerapp.services.interfaces.operationcallback;

import io.github.miguelteles.beststickerapp.exception.StickerException;

public interface OnCreationFinish<T> {

    void onCreationFinish(T createdEntity, StickerException stickerException);

}
