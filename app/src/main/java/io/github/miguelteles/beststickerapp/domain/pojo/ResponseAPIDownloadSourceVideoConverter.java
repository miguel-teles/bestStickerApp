package io.github.miguelteles.beststickerapp.domain.pojo;

public class ResponseAPIDownloadSourceVideoConverter extends ResponseAPIBase {

    private String signedUrl;

    public ResponseAPIDownloadSourceVideoConverter() {
    }

    public ResponseAPIDownloadSourceVideoConverter(String signedUrl) {
        this.signedUrl = signedUrl;
    }

    public String getSignedUrl() {
        return signedUrl;
    }

    public void setSignedUrl(String signedUrl) {
        this.signedUrl = signedUrl;
    }
}
