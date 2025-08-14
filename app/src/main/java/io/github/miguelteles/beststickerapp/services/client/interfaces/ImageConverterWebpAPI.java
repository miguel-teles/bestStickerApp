package io.github.miguelteles.beststickerapp.services.client.interfaces;

import io.github.miguelteles.beststickerapp.domain.pojo.ResponseAPIConvertedWebp;
import io.github.miguelteles.beststickerapp.exception.StickerException;

public interface ImageConverterWebpAPI {

    ResponseAPIConvertedWebp convertImageToWebp(String imageInBase64) throws StickerException;

    void warm() throws StickerException;
}
