package io.github.miguelteles.beststickerapp.domain.pojo;

public class ResponseAPIConvertedWebpDTO extends ResponseAPIBase {
    private final String webpImageBase64;

    public ResponseAPIConvertedWebpDTO(String webpImageBase64) {
        this.webpImageBase64 = webpImageBase64;
    }

    public String getWebpImageBase64() {
        return webpImageBase64;
    }
}