package io.github.miguelteles.beststickerapp.services;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.services.client.ImageConverterWebpAPIImpl;
import io.github.miguelteles.beststickerapp.services.client.interfaces.ImageConverterWebpAPI;
import io.github.miguelteles.beststickerapp.utils.Utils;

public class StickerImageConvertionWarmer {

    private static ImageConverterWebpAPI imageConverterWebpAPI;
    private static Executor executor;

    static {
        try {
            imageConverterWebpAPI = new ImageConverterWebpAPIImpl(Utils.getApplicationContext());
            executor = Executors.newSingleThreadExecutor();
        } catch (StickerException e) {
            throw new RuntimeException(e); //o global exception handler pega
        }
    }

    public static void warm() {
        executor.execute(() -> {
            try {
                imageConverterWebpAPI.warm();
            } catch (StickerException e) {
                throw new RuntimeException(e);
            }

        });
    }
}
