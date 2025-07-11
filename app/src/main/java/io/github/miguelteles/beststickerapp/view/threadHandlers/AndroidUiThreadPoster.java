package io.github.miguelteles.beststickerapp.view.threadHandlers;

import android.os.Handler;
import android.os.Looper;

import io.github.miguelteles.beststickerapp.view.interfaces.UiThreadPoster;

/**
 * Used run the runnable in the main UI thread.
 * **/
public class AndroidUiThreadPoster implements UiThreadPoster {
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void post(Runnable r) {
        handler.post(r);
    }
}
