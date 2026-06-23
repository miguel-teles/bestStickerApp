package io.github.miguelteles.beststickerapp.viewmodel;

import android.net.Uri;

import androidx.annotation.NonNull;

import io.github.miguelteles.beststickerapp.domain.entity.Sticker;
import io.github.miguelteles.beststickerapp.domain.entity.StickerPack;
import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.services.StickerService;
import io.github.miguelteles.beststickerapp.services.interfaces.operationcallback.OperationCallback;
import io.github.miguelteles.beststickerapp.services.mediaconvertion.StickerVideoConvertionService;

public class StickerViewModel {

    private static StickerViewModel instance;
    private final ExecutorWithThreadResultPoster<Sticker> executorWithThreadResultPoster;
    private final StickerService stickerService;

    private StickerViewModel() throws StickerException {
        this.executorWithThreadResultPoster = ExecutorWithThreadResultPoster.getInstance();
        this.stickerService = StickerService.getInstance();
    }

    public void createSticker(@NonNull StickerPack stickerPack,
                              @NonNull Uri stickerImage,
                              @NonNull OperationCallback<Sticker> callbackClass) {
        executorWithThreadResultPoster.execute(
                () -> stickerService.createSticker(stickerPack,
                        stickerImage,
                        callbackClass),
                callbackClass
        );
    }

    public void deleteSticker(Sticker sticker,
                              StickerPack stickerPack) throws StickerException {
        stickerService.deleteSticker(sticker, stickerPack);
    }

    public long getMaxFileSizeAllowed() {
        return StickerVideoConvertionService.MAX_FILE_SIZE_ALLOWED_IN_BYTES;
    }

    public static StickerViewModel getInstance() throws StickerException {
        if (instance == null) {
            instance = new StickerViewModel();
        }
        return instance;
    }
}
