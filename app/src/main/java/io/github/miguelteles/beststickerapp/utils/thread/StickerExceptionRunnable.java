package io.github.miguelteles.beststickerapp.utils.thread;

import io.github.miguelteles.beststickerapp.exception.StickerException;

@FunctionalInterface
public interface StickerExceptionRunnable {

    void run() throws StickerException;

}
