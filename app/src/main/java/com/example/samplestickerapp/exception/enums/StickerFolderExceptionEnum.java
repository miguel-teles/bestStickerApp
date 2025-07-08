package com.example.samplestickerapp.exception.enums;

import com.example.samplestickerapp.R;
import com.example.samplestickerapp.utils.Utils;

public enum StickerFolderExceptionEnum {

    MKDIR_ROOT(R.string.MKDIR_ROOT),

    MKDIR_LOG(R.string.MKDIR_LOG),

    MKDIR_LOG_ERRORS(R.string.MKDIR_LOG_ERRORS),

    MKDIR_LOG_CRITICAL_ERRORS(R.string.MKDIR_LOG_CRITICAL_ERRORS),

    MKDIR_PACKS(R.string.MKDIR_PACKS),

    GET_PATH(R.string.GET_PATH),

    GET_FOLDER(R.string.GET_FOLDER),
    GET_FILE(R.string.GET_FILE),

    CREATE_FOLDER_PACOTE(R.string.CREATE_FOLDER_PACOTE),

    COPY(R.string.COPY),
    RESIZE(R.string.RESIZE),
    DELETE_FOLDER(R.string.DELETE_FOLDER);

    int mResourceId;

    StickerFolderExceptionEnum(int id) {
        this.mResourceId = id;
    }

    public String toString(){
        return Utils.getContext().getString(mResourceId);
    }

}
