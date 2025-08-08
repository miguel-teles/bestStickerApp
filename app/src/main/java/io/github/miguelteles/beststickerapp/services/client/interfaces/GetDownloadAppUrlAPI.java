package io.github.miguelteles.beststickerapp.services.client.interfaces;

import io.github.miguelteles.beststickerapp.domain.pojo.ResponseAPIGetDownloadAppUrl;
import io.github.miguelteles.beststickerapp.exception.StickerException;

public interface GetDownloadAppUrlAPI {

    ResponseAPIGetDownloadAppUrl getDownloadAppUrl(String versionName) throws StickerException;
}
