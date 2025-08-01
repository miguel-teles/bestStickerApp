package io.github.miguelteles.beststickerapp.exception.enums;

import io.github.miguelteles.beststickerapp.R;
import io.github.miguelteles.beststickerapp.utils.Utils;

public enum StickerFatalExceptionEnum {

    NO_SECURE_TOKEN_FOUND(R.string.no_secure_token_found),
    NO_API_ENDPOINT_FOUND(R.string.no_api_endpoint_found);


    final int resourceId;

    StickerFatalExceptionEnum(int id) {
        this.resourceId = id;
    }

    public String toString(){
        return Utils.getApplicationContext().getString(resourceId);
    }

}
