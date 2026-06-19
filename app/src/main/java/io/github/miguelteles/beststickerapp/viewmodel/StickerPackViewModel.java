package io.github.miguelteles.beststickerapp.viewmodel;

import android.net.Uri;

import androidx.annotation.NonNull;

import java.util.List;

import io.github.miguelteles.beststickerapp.domain.entity.StickerPack;
import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.services.StickerPackService;
import io.github.miguelteles.beststickerapp.services.interfaces.operationcallback.OperationCallback;

public class StickerPackViewModel {

    private static StickerPackViewModel instance;
    private final ExecutorWithThreadResultPoster<StickerPack> executorWithThreadResultPoster;
    private final StickerPackService stickerPackService;

    private StickerPackViewModel() throws StickerException {
        this.executorWithThreadResultPoster = ExecutorWithThreadResultPoster.getInstance();
        this.stickerPackService = StickerPackService.getInstance();
    }

    public void createStickerPack(@NonNull String authorName,
                                  @NonNull String packName,
                                  @NonNull Uri imageUri,
                                  boolean isAnimatedStickerPack,
                                  @NonNull OperationCallback<StickerPack> callbackClass) {
        executorWithThreadResultPoster.execute(
                () -> stickerPackService.createStickerPack(authorName,
                        packName,
                        imageUri,
                        isAnimatedStickerPack,
                        callbackClass),
                callbackClass
        );
    }

    public void updateStickerPack(StickerPack stickerPackBeingEdited,
                                  String authorNameInput,
                                  String packNameInput,
                                  OperationCallback<StickerPack> stickerPackCreationCallback) {
        executorWithThreadResultPoster.execute(
                () -> stickerPackService.updateStickerPack(stickerPackBeingEdited,
                        authorNameInput,
                        packNameInput,
                        stickerPackCreationCallback),
                stickerPackCreationCallback
        );
    }

    public StickerPack fetchStickerPackAssets(StickerPack stickerPack) throws StickerException {
        return this.stickerPackService.fetchStickerPackAssets(stickerPack);
    }

    public void deleteStickerPack(StickerPack stickerPack,
                                  OperationCallback<StickerPack> stickerPackDeletionCallback) {
        executorWithThreadResultPoster.execute(
                () -> stickerPackService.deleteStickerPack(stickerPack, stickerPackDeletionCallback),
                stickerPackDeletionCallback
        );
    }

    public static StickerPackViewModel getInstance() throws StickerException {
        if (instance == null) {
            instance = new StickerPackViewModel();
        }
        return instance;
    }

    public List<StickerPack> fetchAllStickerPacksWithAssets() throws StickerException {
        return stickerPackService.fetchAllStickerPacksWithAssets();
    }
}
