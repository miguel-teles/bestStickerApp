package io.github.miguelteles.beststickerapp.services.interfaces;

import android.net.Uri;

import androidx.annotation.NonNull;

import java.util.List;

import io.github.miguelteles.beststickerapp.domain.entity.Sticker;
import io.github.miguelteles.beststickerapp.domain.entity.StickerPack;
import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.exception.StickerFolderException;

public interface StickerService {

    Sticker createSticker(StickerPack stickerPack,
                          Uri uriStickerImage) throws StickerException;

    void deleteSticker(Sticker sticker,
                       StickerPack stickerPack) throws StickerException;

    void deleteStickersFromStickerPack(Integer packIdentifier) throws StickerException;

    List<Sticker> fetchAllStickerFromPackWithAssets(Integer packIdentifier) throws StickerException;
    List<Sticker> fetchAllStickerFromPackWithoutAssets(Integer packIdentifier) throws StickerException;


    /**
     * Busca um asset da pasta (imagem da figurinha ou da capa do sticker pack)
     **/
    byte[] fetchStickerAsset(@NonNull Integer packIdentifier,
                             @NonNull String stickerImageFileName) throws StickerFolderException;
}
