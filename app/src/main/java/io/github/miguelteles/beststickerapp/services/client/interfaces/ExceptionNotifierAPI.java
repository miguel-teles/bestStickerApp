package io.github.miguelteles.beststickerapp.services.client.interfaces;

import io.github.miguelteles.beststickerapp.domain.pojo.ResponseAPIBase;
import io.github.miguelteles.beststickerapp.exception.StickerException;

public interface ExceptionNotifierAPI {

    ResponseAPIBase post(String exceptionNotification) throws StickerException;
}
