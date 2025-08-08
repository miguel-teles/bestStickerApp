package io.github.miguelteles.beststickerapp.domain.pojo;

public class ResponseAPIGetDownloadAppUrl extends ResponseAPIBase {

    private String presignedUrl;

    public ResponseAPIGetDownloadAppUrl() {
    }

    public String getPresignedUrl() {
        return presignedUrl;
    }

    public void setPresignedUrl(String presignedUrl) {
        this.presignedUrl = presignedUrl;
    }
}
