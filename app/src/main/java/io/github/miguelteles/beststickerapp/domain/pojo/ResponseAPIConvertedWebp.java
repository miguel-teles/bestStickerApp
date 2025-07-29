package io.github.miguelteles.beststickerapp.domain.pojo;

public class ResponseAPIConvertedWebp extends ResponseAPIBase {
    private final String webpImageBase64;

    public ResponseAPIConvertedWebp(String webpImageBase64) {
        this.webpImageBase64 = webpImageBase64;
    }

    public String getWebpImageBase64() {
        return webpImageBase64;
    }
}