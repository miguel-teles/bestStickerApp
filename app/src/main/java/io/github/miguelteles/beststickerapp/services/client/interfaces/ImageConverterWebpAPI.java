package io.github.miguelteles.beststickerapp.services.client.interfaces;

import io.github.miguelteles.beststickerapp.domain.pojo.ResponseAPIConvertedWebpDTO;
import io.github.miguelteles.beststickerapp.exception.StickerHttpClientException;

public interface ImageConverterWebpAPI {

    ResponseAPIConvertedWebpDTO convertImageToWebp(String imageInBase64) throws StickerHttpClientException;
}
