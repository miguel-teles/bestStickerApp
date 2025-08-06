/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.miguelteles.beststickerapp.domain.entity;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import io.github.miguelteles.beststickerapp.utils.Utils;
import kotlin.jvm.Transient;

public class Sticker implements Parcelable, Cloneable {
    public final static String NM_TABELA = "stickers";
    public static String IDENTIFIER = "identifier";
    public static String PACK_IDENTIFIER = "packIdentifier";
    public static String STICKER_IMAGE_FILE = "stickerImageFile";
    public static final int STICKER_IMAGE_SIZE = 512; //512pxs
    public static final String STICKER_ERROR_IMAGE = "sticker_error_image.webp";


    private UUID identifier;
    private UUID packIdentifier;
    private final String stickerImageFile;
    private final List<String> emojis;
    private long size;
    private byte[] stickerImageFileInBytes;
    private String accessibilityText;

    public Sticker(String stickerImageFile,
                   List<String> emojis,
                   String accessibilityText,
                   UUID identifier,
                   UUID packIdentifier) {
        this.stickerImageFile = stickerImageFile;
        this.emojis = emojis;
        this.accessibilityText = accessibilityText;
        this.identifier = identifier;
        this.packIdentifier= packIdentifier;
    }

    public Sticker(UUID identifier,
                   UUID packIdentifier,
                   String stickerImageFile) {
        this.identifier = identifier;
        this.packIdentifier = packIdentifier;
        this.stickerImageFile = stickerImageFile;
        this.emojis = new ArrayList<>();
    }

    public Sticker(String stickerImageFile,
                   UUID packIdentifier,
                   byte[] stickerImageFileInBytes) {
        this.stickerImageFile = stickerImageFile;
        this.emojis = new ArrayList<>();
        this.packIdentifier = packIdentifier;
        this.stickerImageFileInBytes = stickerImageFileInBytes;
        this.size = this.stickerImageFileInBytes.length;
    }

    private Sticker(Parcel in) {
        stickerImageFile = in.readString();
        emojis = in.createStringArrayList();
        size = in.readLong();
    }

    public static final Creator<Sticker> CREATOR = new Creator<>() {
        @Override
        public Sticker createFromParcel(Parcel in) {
            return new Sticker(in);
        }

        @Override
        public Sticker[] newArray(int size) {
            return new Sticker[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(stickerImageFile);
        dest.writeStringList(emojis);
        dest.writeLong(size);
    }

    @Override
    public Sticker clone() {
        try {
            Sticker clone = (Sticker) super.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public String getStickerImageFile() {
        return stickerImageFile;
    }
    public List<String> getEmojis() {
        return emojis;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public UUID getIdentifier() {
        return identifier;
    }

    public void setIdentifier(UUID identifier) {
        if (this.identifier == null) {
            this.identifier = identifier;
        }
    }

    public UUID getPackIdentifier() {
        return packIdentifier;
    }

    public void setPackIdentifier(UUID packIdentifier) {
        if (this.packIdentifier == null) {
            this.packIdentifier = packIdentifier;
        }
    }

    public byte[] getStickerImageFileInBytes() {
        return stickerImageFileInBytes;
    }

    public void setStickerImageFileInBytes(byte[] stickerImageFileInBytes) {
        this.stickerImageFileInBytes = stickerImageFileInBytes;
    }

    public String getAccessibilityText() {
        return accessibilityText;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Sticker sticker = (Sticker) o;
        return size == sticker.size &&
                Objects.equals(identifier, sticker.identifier) &&
                Objects.equals(packIdentifier, sticker.packIdentifier) &&
                Objects.equals(stickerImageFile, sticker.stickerImageFile);
    }

}
