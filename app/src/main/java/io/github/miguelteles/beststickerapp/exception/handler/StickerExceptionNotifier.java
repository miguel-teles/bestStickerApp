package io.github.miguelteles.beststickerapp.exception.handler;

public interface StickerExceptionNotifier {

    void initNotifying();

    void addExceptionToNotificationQueue(Throwable exception);
}
