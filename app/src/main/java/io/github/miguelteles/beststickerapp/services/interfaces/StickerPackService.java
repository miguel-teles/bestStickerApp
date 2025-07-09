package io.github.miguelteles.beststickerapp.services.interfaces;

import android.content.Context;
import android.net.Uri;

import java.util.List;

import io.github.miguelteles.beststickerapp.domain.entity.StickerPack;

public interface StickerPackService {

    StickerPack createStickerPack(String nomeAutor,
                                  String nomePacote,
                                  Uri uriImagemStickerPackInput,
                                  Context context);

    StickerPack updateStickerPack(StickerPack stickerPack,
                                  String nomeAutor,
                                  String nomePacote,
                                  Context context);

    void deleteStickerPack(StickerPack stickerPack, Context context);

    StickerPack fetchUpdatedStickerPack(StickerPack stickerPack);

    List<StickerPack> fetchAllStickerPacks();
}
