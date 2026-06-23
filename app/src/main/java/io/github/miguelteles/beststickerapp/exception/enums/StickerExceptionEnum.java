package io.github.miguelteles.beststickerapp.exception.enums;

import io.github.miguelteles.beststickerapp.R;
import io.github.miguelteles.beststickerapp.utils.Utils;

public enum StickerExceptionEnum {

    CSP(R.string.CREATE_STICKER_PACK),
    CS(R.string.CREATE_STICKER),
    ESP(R.string.EDIT_STICKER_PACK),
    FS(R.string.FETCH_STICKER),
    FSP(R.string.FETCH_STICKER_PACK),
    ISP(R.string.INVALID_STICKER_PACK),
    GUL(R.string.GET_UPDATE_LINK),
    END(R.string.NOT_DEFINED_EXCEPTION),
    WTSP(R.string.WRONG_TYPE_STICKER_PACK),
    IDL(R.string.INVALID_DOWNLOAD_LINK),
    CTO(R.string.CONVERTION_TIME_OUT);


    final int resourceId;

    StickerExceptionEnum(int id) {
        this.resourceId = id;
    }

    public String toString(){
        return Utils.getApplicationContext().getString(resourceId);
    }
}
