package com.example.samplestickerapp.exception.enums;

import com.example.samplestickerapp.R;
import com.example.samplestickerapp.utils.Utils;

public enum StickerExceptionEnum {

    CSP(R.string.CREATE_STICKER_PACK),
    ESP(R.string.EDIT_STICKER_PACK);


    int mResourceId;

    StickerExceptionEnum(int id) {
        this.mResourceId = id;
    }

    public String toString(){
        return Utils.getContext().getString(mResourceId);
    }
}
