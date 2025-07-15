package io.github.miguelteles.beststickerapp.services.interfaces;

import io.github.miguelteles.beststickerapp.exception.StickerException;

public interface EntityOperationCallback<T> {
    void onCreationFinish(T createdEntity, StickerException stickerException);

    void onProgressUpdate(int process);

    void runProgressBarAnimation(int process);
}