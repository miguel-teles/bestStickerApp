package io.github.miguelteles.beststickerapp.services.interfaces;

import android.net.Uri;

import java.io.File;

import io.github.miguelteles.beststickerapp.exception.StickerException;

public interface DownloadCallback {

    void onProgressFinish(File file);
    void onProgressUpdate(int process);

    void onDownloadException(StickerException ex);
}
