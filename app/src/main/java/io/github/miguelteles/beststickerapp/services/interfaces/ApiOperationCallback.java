package io.github.miguelteles.beststickerapp.services.interfaces;

import io.github.miguelteles.beststickerapp.exception.StickerException;

public interface ApiOperationCallback<T> {
    void onCreationFinish(T createdEntity, StickerException stickerException);
}
