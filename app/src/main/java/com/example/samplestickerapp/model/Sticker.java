/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.example.samplestickerapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class Sticker implements Parcelable {
    public final static String NM_TABELA = "stickers";
    private Integer identifier; //PK
    private Integer packIdentifier; //FK
    private final String imageFileName;
    private final List<String> emojis;
    private long size;

    public Sticker(String imageFileName, List<String> emojis, Integer packIdentifier) {
        this.imageFileName = imageFileName;
        this.emojis = emojis;
        this.packIdentifier = packIdentifier;
    }

    private Sticker(Parcel in) {
        imageFileName = in.readString();
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

    public void setSize(long size) {
        this.size = size;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(imageFileName);
        dest.writeStringList(emojis);
        dest.writeLong(size);
    }

    public String getImageFileName() {
        return imageFileName;
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
