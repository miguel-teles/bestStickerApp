package io.github.miguelteles.beststickerapp.services.interfaces.operationcallback;

import io.github.miguelteles.beststickerapp.exception.StickerException;

public interface OperationCallback<T> extends OnCreationFinish<T>, OnProgressUpdate{

    static <T> OperationCallback<T> getDefault() {
        return new OperationCallback<T>() {
            @Override
            public void onCreationFinish(T createdEntity, StickerException stickerException) {

            }

            @Override
            public void onProgressUpdate(int process) {

            }

            @Override
            public void onProgressUpdate() {

            }
        };
    }

}