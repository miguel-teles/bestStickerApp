package io.github.miguelteles.beststickerapp.services.client.interfaces;

import io.github.miguelteles.beststickerapp.domain.pojo.ResponseAPIAppLatestVersion;
import io.github.miguelteles.beststickerapp.exception.StickerException;

public interface GetLatestAppVersionAPI {

    ResponseAPIAppLatestVersion get() throws StickerException;

}
