package io.github.miguelteles.beststickerapp.view.threadHandlers;

import io.github.miguelteles.beststickerapp.view.interfaces.UiThreadPoster;

/**
 * Used in unitTests to run the runnable in the same thread as the caller
 * **/
public class ImmediateUiThreadPoster implements UiThreadPoster {
    @Override
    public void post(Runnable r) {
        r.run();
    }
}
