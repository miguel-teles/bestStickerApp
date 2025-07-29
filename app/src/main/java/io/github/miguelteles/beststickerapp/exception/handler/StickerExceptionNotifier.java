package io.github.miguelteles.beststickerapp.exception.handler;

public interface StickerExceptionNotifier {

    void initNotifying();

    void writeExceptionIntoLogFile(Throwable exception);
}
