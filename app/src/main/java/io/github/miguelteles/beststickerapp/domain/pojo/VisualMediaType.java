package io.github.miguelteles.beststickerapp.domain.pojo;

public enum VisualMediaType {
    IMAGE,
    VIDEO;

    public boolean isImage() {
        return this.equals(IMAGE);
    }

    public boolean isVideo() {
        return this.equals(VIDEO);
    }
}
