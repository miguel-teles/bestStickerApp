package io.github.miguelteles.beststickerapp.services.client.interfaces;

import java.io.File;

import io.github.miguelteles.beststickerapp.domain.pojo.ResponseAPIDownloadSourceVideoConverter;
import io.github.miguelteles.beststickerapp.domain.pojo.ResponseAPIUploadDestinationVideoConverter;
import io.github.miguelteles.beststickerapp.exception.StickerException;

public interface VideoConverterWebpAPI {

    ResponseAPIUploadDestinationVideoConverter createUploadDestination(String filename) throws StickerException;
    ResponseAPIDownloadSourceVideoConverter createDownloadSource(String filename) throws StickerException;

    Object uploadVideo(String presignedUrl, File video) throws StickerException;

    void warm() throws StickerException;

}
