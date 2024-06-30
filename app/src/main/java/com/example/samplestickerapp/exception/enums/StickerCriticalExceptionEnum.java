package com.example.samplestickerapp.exception.enums;

public enum StickerCriticalExceptionEnum {

    MKDIR_ROOT("Erro ao criar pasta dos arquivos do aplicativo"),

    MKDIR_LOG("Erro ao criar pasta de logs"),

    MKDIR_LOG_ERRORS("Erro ao criar pasta de logs de erro"),

    MKDIR_LOG_CRITICAL_ERRORS("Erro ao criar pasta de logs de erros críticos"),

    MKDIR_PACKS("Erro ao criar pasta de pacote(s)"),

    GET_PATH("Erro ao encontrar local do arquivo"),

    GET_FOLDER("Erro ao acessar pasta"),
    GET_FILE("Erro ao abrir arquivo"),

    CREATE_FOLDER_PACOTE("Erro ao criar pasta do pacote de figurinhas"),

    COPY("Erro ao copiar arquivo ao destino novo"),
    RESIZE("Erro ao mudar tamanho da imagem");

    String txt;

    StickerCriticalExceptionEnum(String s) {
        this.txt = s;
    }

    public String toString(){
        return txt;
    }

}
