package io.github.miguelteles.beststickerapp.exception.enums;

import io.github.miguelteles.beststickerapp.R;
import io.github.miguelteles.beststickerapp.utils.Utils;

public enum StickerWebCommunicationExceptionEnum {

    POST(R.string.POST),
    NO_INTERNET_ACCESS(R.string.NO_INTERNET_ACCESS),
    DOWNLOAD_UPDATE_EXCEPTION(R.string.DOWNLOAD_UPDATE_EXCEPTION),
    TIMEOUT(R.string.TIMEOUT);

    int mResourceId;

    StickerWebCommunicationExceptionEnum(int id) {
        this.mResourceId = id;
    }

    public String toString(){
        return Utils.getApplicationContext().getString(mResourceId);
    }

}
