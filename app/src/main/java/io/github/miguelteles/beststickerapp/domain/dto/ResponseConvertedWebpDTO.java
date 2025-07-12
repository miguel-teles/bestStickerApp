package io.github.miguelteles.beststickerapp.domain.dto;

public class ResponseConvertedWebpDTO {
    private final String webpImageBase64;

    public ResponseConvertedWebpDTO(String webpImageBase64) {
        this.webpImageBase64 = webpImageBase64;
    }

    public String getWebpImageBase64() {
        return webpImageBase64;
    }
}