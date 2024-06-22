package com.example.samplestickerapp.exception.enums;

public enum StickerExceptionEnum {

    CSP("Erro criar novo pacote de figurinhas");


    String text;
    StickerExceptionEnum(String str) {
        this.text = str;
    }

}
