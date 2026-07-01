package io.github.miguelteles.beststickerapp.services.mediaconvertion;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.services.client.VideoConverterWebpAPIImpl;
import io.github.miguelteles.beststickerapp.services.client.interfaces.VideoConverterWebpAPI;

public class StickerVideoConvertionWarmer {

    private static final VideoConverterWebpAPI videoConverterWebpAPI;
    private static final Executor executor;

    static {
        try {
            videoConverterWebpAPI = VideoConverterWebpAPIImpl.getInstance();
            executor = Executors.newSingleThreadExecutor();
        } catch (StickerException e) {
            throw new RuntimeException(e); //o global exception handler pega
        }
    }

    public static void warm() {
        executor.execute(() -> {
            try {
                videoConverterWebpAPI.warm();
            } catch (StickerException e) {
                throw new RuntimeException(e);
            }

        });
    }
}
