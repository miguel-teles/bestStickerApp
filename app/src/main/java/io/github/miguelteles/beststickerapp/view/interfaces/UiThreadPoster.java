package io.github.miguelteles.beststickerapp.view.interfaces;

/**
 * This interface abstract the new Handler(Looper.getMainLooper()) call so that the services can go through
 * unit tests by disabling the thread jumping.
 *
 * The newHandler(Looper.getMainLooper()) creates a Handler that posts tasks to the main UI thread, sending the "result" from one thread process
 * to the main UI thread. Since the unit tests doesn't have that thread, this interface exists so that a test implementation can get the results in a synchronous way.
 * **/
public interface UiThreadPoster {
    void post(Runnable r);
}
