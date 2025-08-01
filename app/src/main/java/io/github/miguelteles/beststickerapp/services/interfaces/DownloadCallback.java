package io.github.miguelteles.beststickerapp.services.interfaces;

import io.github.miguelteles.beststickerapp.exception.StickerException;

public interface DownloadCallback {

    void onProgressUpdate(int process);

    void onDownloadException(StickerException ex);
}
