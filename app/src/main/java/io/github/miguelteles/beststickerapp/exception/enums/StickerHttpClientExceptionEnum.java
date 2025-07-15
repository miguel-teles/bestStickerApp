package io.github.miguelteles.beststickerapp.exception.enums;

import io.github.miguelteles.beststickerapp.R;
import io.github.miguelteles.beststickerapp.utils.Utils;

public enum StickerHttpClientExceptionEnum {

    POST(R.string.POST);

    int mResourceId;

    StickerHttpClientExceptionEnum(int id) {
        this.mResourceId = id;
    }

    public String toString(){
        return Utils.getApplicationContext().getString(mResourceId);
    }

}
