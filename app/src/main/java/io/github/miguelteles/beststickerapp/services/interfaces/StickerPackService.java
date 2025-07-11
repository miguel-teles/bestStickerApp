package io.github.miguelteles.beststickerapp.services.interfaces;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.util.List;

import io.github.miguelteles.beststickerapp.domain.entity.StickerPack;
import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.exception.StickerFolderException;

public interface StickerPackService {

    StickerPack createStickerPack(String authorName,
                                  String packName,
                                  Uri selectedImagemUri) throws StickerException;

    StickerPack updateStickerPack(StickerPack stickerPack,
                                  String editedAuthorName,
                                  String editedPackName) throws StickerException;

    void deleteStickerPack(StickerPack stickerPack) throws StickerException;

    StickerPack fetchStickerPackByIdWithAssets(StickerPack stickerPack) throws StickerException;

    List<StickerPack> fetchAllStickerPacksWithAssets() throws StickerException;
    List<StickerPack> fetchAllStickerPacksWithoutAssets() throws StickerException;

    byte[] fetchStickerPackAsset(@NonNull Integer packIdentifier, @NonNull String stickerPackImageFileName) throws StickerFolderException;
}
