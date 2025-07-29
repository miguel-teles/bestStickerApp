package io.github.miguelteles.beststickerapp.exception.enums;

import io.github.miguelteles.beststickerapp.R;
import io.github.miguelteles.beststickerapp.utils.Utils;

public enum StickerFatalExceptionEnum {

    NO_SECURE_TOKEN_FOUND(R.string.NO_SECURE_TOKEN_FOUND),
    NO_API_ENDPOINT_FOUND(R.string.NO_API_ENDPOINT_FOUND);


    final int resourceId;

    StickerFatalExceptionEnum(int id) {
        this.resourceId = id;
    }

    public String toString(){
        return Utils.getApplicationContext().getString(resourceId);
    }

}
