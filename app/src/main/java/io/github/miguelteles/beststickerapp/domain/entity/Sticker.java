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

import java.util.ArrayList;
import java.util.List;

public class Sticker implements Parcelable {
    public final static String NM_TABELA = "stickers";

    public static String IDENTIFIER = "identifier";
    public static String PACK_IDENTIFIER = "packIdentifier";
    public static String STICKER_IMAGE_FILE = "stickerImageFile";
    public static String EMOJIS = "emojis";


    private Integer identifier; //PK
    private Integer packIdentifier; //FK
    private final String stickerImageFile;
    private final List<String> emojis;
    private long size;

    public Sticker(Integer identifier,
                   Integer packIdentifier,
                   String stickerImageFile) {
        this.identifier = identifier;
        this.packIdentifier = packIdentifier;
        this.stickerImageFile = stickerImageFile;
        this.emojis = new ArrayList<>();
    }

    public Sticker(String stickerImageFile,
                   Integer packIdentifier,
                   long size) {
        this.stickerImageFile = stickerImageFile;
        this.emojis = new ArrayList<>();
        this.packIdentifier = packIdentifier;
        this.size = size;
    }

    private Sticker(Parcel in) {
        stickerImageFile = in.readString();
        emojis = in.createStringArrayList();
        size = in.readLong();
    }

    public static final Creator<Sticker> CREATOR = new Creator<Sticker>() {
        @Override
        public Sticker createFromParcel(Parcel in) {
            return new Sticker(in);
        }

        @Override
        public Sticker[] newArray(int size) {
            return new Sticker[size];
        }
    };

    public static Sticker fromContentValues(ContentValues values) {
        return new Sticker(values.getAsInteger(IDENTIFIER),
                values.getAsInteger(PACK_IDENTIFIER),
                values.getAsString(STICKER_IMAGE_FILE));
    }

    public ContentValues toContentValues() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(IDENTIFIER, this.getIdentifier());
        contentValues.put(PACK_IDENTIFIER, this.getPackIdentifier());
        contentValues.put(STICKER_IMAGE_FILE, this.getStickerImageFile());
        return contentValues;
    }

    public void setSize(long size) {
        this.size = size;
    }

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

    public String getStickerImageFile() {
        return stickerImageFile;
    }

    public List<String> getEmojis() {
        return emojis;
    }

    public long getSize() {
        return size;
    }

    public Integer getIdentifier() {
        return identifier;
    }

    public void setIdentifier(Integer identifier) {
        this.identifier = identifier;
    }

    public Integer getPackIdentifier() {
        return packIdentifier;
    }

    public void setPackIdentifier(Integer packIdentifier) {
        this.packIdentifier = packIdentifier;
    }
}
