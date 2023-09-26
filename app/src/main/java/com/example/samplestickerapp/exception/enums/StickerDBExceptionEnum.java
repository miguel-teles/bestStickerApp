package com.example.samplestickerapp.exception.enums;

public enum StickerDBExceptionEnum {

    CREATE_OR_OPEN("Erro ao abrir ou criar banco de dados das figurinhas"),

    INI("Erro ao inicializar banco e tabelas"),

    CREATE_TBL("Erro ao criar tabela"),

    INSERT("Erro ao inserir dados na tabela"),

    SELECT("Erro ao buscar valores");

    private String text;

    StickerDBExceptionEnum(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
