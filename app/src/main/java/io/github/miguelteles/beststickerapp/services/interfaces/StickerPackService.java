package io.github.miguelteles.beststickerapp.services.interfaces;

import android.content.Context;
import android.net.Uri;

import java.util.List;

import io.github.miguelteles.beststickerapp.domain.entity.StickerPack;
import io.github.miguelteles.beststickerapp.exception.StickerException;

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

    StickerPack fetchUpdatedStickerPack(StickerPack stickerPack) throws StickerException;

    List<StickerPack> fetchAllStickerPacks() throws StickerException;
}
