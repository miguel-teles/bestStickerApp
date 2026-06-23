package io.github.miguelteles.beststickerapp.viewmodel;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.services.interfaces.operationcallback.OperationCallback;
import io.github.miguelteles.beststickerapp.utils.thread.StickerExceptionSupplier;
import io.github.miguelteles.beststickerapp.utils.thread.StickerExceptionRunnable;
import io.github.miguelteles.beststickerapp.view.interfaces.UiThreadPoster;
import io.github.miguelteles.beststickerapp.view.threadHandlers.AndroidUiThreadPoster;

class ExecutorWithThreadResultPoster<T> {

    private static ExecutorWithThreadResultPoster instance;

    private final Executor executor;
    private final UiThreadPoster threadResultPoster;

    private ExecutorWithThreadResultPoster() {
        this.executor = Executors.newSingleThreadExecutor();
        this.threadResultPoster = new AndroidUiThreadPoster();
    }

    public ExecutorWithThreadResultPoster(Executor executor, UiThreadPoster threadResultPoster) {
        this.executor = executor;
        this.threadResultPoster = threadResultPoster;
    }

    public static <T> ExecutorWithThreadResultPoster<T> getInstance() {
        if (instance == null) {
            instance = new ExecutorWithThreadResultPoster<T>();
        }
        return instance;
    }

    public void execute(StickerExceptionSupplier<T> supplier,
                        OperationCallback<T> stickerCreationCallback) {
        executor.execute(() -> {
            StickerException exception = null;
            T result = null;
            try {
                result = supplier.get();
            } catch (StickerException ex) {
                exception = ex;
            }
            postResult(stickerCreationCallback, exception, result);
        });
    }

    public void execute(StickerExceptionRunnable runnable,
                        OperationCallback<T> stickerCreationCallback) {
        executor.execute(() -> {
            StickerException exception = null;
            try {
                runnable.run();
            } catch (StickerException ex) {
                exception = ex;
            }
            postResult(stickerCreationCallback, exception, null);
        });
    }

    private void postResult(OperationCallback<T> stickerCreationCallback,
                            StickerException exception,
                            T result) {
        threadResultPoster.post(() -> stickerCreationCallback.onCreationFinish(result, exception));
    }

}
