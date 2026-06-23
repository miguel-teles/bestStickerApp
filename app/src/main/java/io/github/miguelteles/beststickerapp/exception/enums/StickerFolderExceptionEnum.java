package io.github.miguelteles.beststickerapp.exception.enums;

import io.github.miguelteles.beststickerapp.R;
import io.github.miguelteles.beststickerapp.utils.Utils;

public enum StickerFolderExceptionEnum {

    MKDIR(R.string.MKDIR),
    MKFILE(R.string.MKFILE),

    MKDIR_LOG(R.string.MKDIR_LOG),

    MKDIR_LOG_ERRORS(R.string.MKDIR_LOG_ERRORS),

    MKDIR_LOG_CRITICAL_ERRORS(R.string.MKDIR_LOG_CRITICAL_ERRORS),

    MKDIR_PACKS(R.string.MKDIR_PACKS),
    GET_PATH(R.string.GET_PATH),
    GET_FOLDER(R.string.GET_FOLDER),
    GET_FILE(R.string.GET_FILE),
    GET_FILE_TYPE(R.string.GET_FILE_TYPE),
    CREATE_FOLDER_PACOTE(R.string.CREATE_FOLDER_PACOTE),

    COPY(R.string.COPY),
    RESIZE(R.string.RESIZE),
    DELETE_FOLDER(R.string.DELETE_FOLDER),

    CONVERT_FILE(R.string.CONVERT_FILE),
    DOWNLOAD(R.string.DOWNLOAD),
    FILE_NOT_FOUND(R.string.FILE_NOT_FOUND);

    int mResourceId;

    StickerFolderExceptionEnum(int id) {
        this.mResourceId = id;
    }

    public String toString(){
        return Utils.getApplicationContext().getString(mResourceId);
    }

}
