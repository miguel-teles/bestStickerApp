package com.example.samplestickerapp.exception.enums;

public enum StickerCriticalExceptionEnum {

    MKDIR_ROOT("Erro ao criar pasta dos arquivos do aplicativo"),

    MKDIR_LOG("Erro ao criar pasta de logs"),

    MKDIR_LOG_ERRORS("Erro ao criar pasta de logs de erro"),

    MKDIR_LOG_CRITICAL_ERRORS("Erro ao criar pasta de logs de erros críticos"),

    MKDIR_PACKS("Erro ao criar pasta de packs"),

    GET_PATH("Erro ao encontrar local do arquivo");

    String txt;

    StickerCriticalExceptionEnum(String s) {
        this.txt = s;
    }

    public String toString(){
        return txt;
    }

}
