package io.github.miguelteles.beststickerapp.exception.enums;

import io.github.miguelteles.beststickerapp.R;
import io.github.miguelteles.beststickerapp.utils.Utils;

public enum StickerExceptionEnum {

    CSP(R.string.CREATE_STICKER_PACK),
    CS(R.string.CREATE_STICKER),
    ESP(R.string.EDIT_STICKER_PACK);


    final int resourceId;

    StickerExceptionEnum(int id) {
        this.resourceId = id;
    }

    public String toString(){
        return Utils.getContext().getString(resourceId);
    }
}
