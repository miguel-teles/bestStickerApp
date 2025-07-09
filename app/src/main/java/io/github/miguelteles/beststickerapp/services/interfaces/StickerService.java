package io.github.miguelteles.beststickerapp.services.interfaces;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import io.github.miguelteles.beststickerapp.domain.entity.Sticker;
import io.github.miguelteles.beststickerapp.domain.entity.StickerPack;
import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.repository.contentProvider.StickerContentProviderReader;
import io.github.miguelteles.beststickerapp.repository.contentProvider.StickerUriProvider;

public interface StickerService {

    Sticker createSticker(StickerPack stickerPack,
                          Uri uriStickerImage,
                          Context context) throws StickerException;

    void deleteSticker(Sticker sticker,
                       StickerPack stickerPack,
                       Context context) throws StickerException;

    List<Sticker> fetchAllStickerFromPack(StickerPack stickerPack, Context context) throws StickerException ;

    /**
     * Busca um asset da pasta (imagem da figurinha ou da capa do sticker pack)
     **/
    static byte[] fetchStickerAsset(@NonNull String identifier,
                                           @NonNull String stickerImageFileName,
                                           ContentResolver contentResolver) throws IOException {
        /**
         * Busca um asset da pasta (imagem da figurinha ou da capa do sticker pack)
         * **/
        //o contentResolver.openInputStream vai pro método openAssetFile do contentProvider
        try (final InputStream inputStream = contentResolver.openInputStream(StickerUriProvider.getStickerAssetUri(identifier, stickerImageFileName));
             final ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            if (inputStream == null) {
                throw new IOException("cannot read sticker asset id: " + identifier + "; name: " + stickerImageFileName);
            }
            int read;
            byte[] data = new byte[16384];

            while ((read = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, read);
            }
            return buffer.toByteArray();
        } catch (IOException ex) {
            throw ex;
        }
    }
}
