package io.github.miguelteles.beststickerapp.services.interfaces;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.util.List;

import io.github.miguelteles.beststickerapp.domain.entity.StickerPack;
import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.exception.StickerFolderException;

public interface StickerPackService {

    StickerPack createStickerPack(String nomeAutor,
                                  String nomePacote,
                                  Uri uriImagemStickerPackInput,
                                  Context context) throws StickerException;

    StickerPack updateStickerPack(StickerPack stickerPack,
                                  String nomeAutor,
                                  String nomePacote,
                                  Context context) throws StickerException;

    void deleteStickerPack(StickerPack stickerPack, Context context) throws StickerException;

    StickerPack fetchStickerPackByIdWithAssets(StickerPack stickerPack) throws StickerException;

    List<StickerPack> fetchAllStickerPacksWithAssets() throws StickerException;
    List<StickerPack> fetchAllStickerPacksWithoutAssets() throws StickerException;

    byte[] fetchStickerPackAsset(@NonNull Integer packIdentifier, @NonNull String stickerPackImageFileName) throws StickerFolderException;
}
