package io.github.miguelteles.beststickerapp.domain.pojo;

public class ResponseAPIUploadDestinationVideoConverter extends ResponseAPIBase {

    private String signedUrl;
    private String convertedFileName;

    public ResponseAPIUploadDestinationVideoConverter() {
    }

    public ResponseAPIUploadDestinationVideoConverter(String signedUrl, String convertedFileName) {
        this.signedUrl = signedUrl;
        this.convertedFileName = convertedFileName;
    }

    public String getSignedUrl() {
        return signedUrl;
    }

    public void setSignedUrl(String signedUrl) {
        this.signedUrl = signedUrl;
    }

    public String getConvertedFileName() {
        return convertedFileName;
    }

    public void setConvertedFileName(String convertedFileName) {
        this.convertedFileName = convertedFileName;
    }
}
