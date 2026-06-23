package io.github.miguelteles.beststickerapp.domain.pojo;

public enum VisualMediaType {
    IMAGE,
    VIDEO,
    GIF;

    public boolean isImage() {
        return this.equals(IMAGE);
    }

    public boolean isVideo() {
        return this.equals(VIDEO);
    }

    public boolean isAnimated() {
        return this.isGif() || this.isVideo();
    }

    public boolean isGif() { return this.equals(GIF); }
}
