package io.github.miguelteles.beststickerapp.exception.enums;

public enum StickerDataBaseExceptionEnum {

    CREATE_OR_OPEN("Erro ao abrir ou criar banco de dados das figurinhas"),

    INI("Erro ao inicializar banco e tabelas"),

    CREATE_TBL("Erro ao criar tabela"),

    INSERT("Erro ao inserir dados na tabela"),

    SELECT("Erro ao buscar valores"),

    UPDATE("Erro ao atualizar valores"),

    DELETE("Erro ao deletar valores");

    private String text;

    StickerDataBaseExceptionEnum(String text) {
        this.text = text;
    }

    public String toString() {
        return text;
    }
}
