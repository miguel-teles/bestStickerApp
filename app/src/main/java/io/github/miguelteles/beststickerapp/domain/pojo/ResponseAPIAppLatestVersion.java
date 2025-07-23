package io.github.miguelteles.beststickerapp.domain.pojo;

public class ResponseAPIAppLatestVersion extends ResponseAPIBase {

    private Version latestVersion;

    public Version getLatestVersion() {
        return latestVersion;
    }

    public void setLatestVersion(Version latestVersion) {
        this.latestVersion = latestVersion;
    }
}
