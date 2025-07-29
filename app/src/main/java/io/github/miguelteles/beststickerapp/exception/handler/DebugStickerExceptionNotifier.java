package io.github.miguelteles.beststickerapp.exception.handler;

import io.github.miguelteles.beststickerapp.exception.StickerException;

public class DebugStickerExceptionNotifier extends ProductionStickerExceptionNotifier {

    public DebugStickerExceptionNotifier() throws StickerException {
        super();
    }

    @Override
    public void initNotifying() {
    }
}
