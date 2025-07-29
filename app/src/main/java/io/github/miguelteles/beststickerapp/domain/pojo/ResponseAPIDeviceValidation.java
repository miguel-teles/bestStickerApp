package io.github.miguelteles.beststickerapp.domain.pojo;

public class ResponseAPIDeviceValidation extends ResponseAPIBase {

    private String clientCertificate;

    public String getClientCertificate() {
        return clientCertificate;
    }

    public void setClientCertificate(String clientCertificate) {
        this.clientCertificate = clientCertificate;
    }
}
