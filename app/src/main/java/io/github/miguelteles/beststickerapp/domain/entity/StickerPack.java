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

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import io.github.miguelteles.beststickerapp.utils.Utils;

public class StickerPack implements Parcelable {

    public static final String NM_TABELA = "packs";
    public static final String IDENTIFIER = "identifier";
    public static final String NAME = "name";
    public static final String PUBLISHER = "publisher";
    public static final String ORIGINAL_TRAY_IMAGE_FILE = "originalTrayImageFile";
    public static final String RESIZED_TRAY_IMAGE_FILE = "resizedTrayImageFile";
    public static final String IMAGE_DATA_VERSION = "imageDataVersion";
    public static final String FOLDER = "folder";
    public static final String AVOID_CACHE = "avoidCache";
    public static final String ANIMATED_STICKER_PACK = "animatedStickerPack";
    public static final int TRAY_IMAGE_SIZE = 96; //96pxs

    private UUID identifier;
    private String name;
    private String publisher;
    private String originalTrayImageFile;
    private String resizedTrayImageFile;
    private Integer imageDataVersion;
    private final String folderName;
    private final boolean avoidCache;
    private final boolean animatedStickerPack;
    private String iosAppStoreLink;
    private String androidPlayStoreLink;
    private List<Sticker> stickers;
    private long totalSize;
    private boolean isWhitelisted;
    private byte[] resizedTrayImageFileInBytes;


    /*
    * "identifier": "1",
      "name": "Cuppy",
      "publisher": "Jane Doe",
      "tray_image_file": "tray_Cuppy.png",
      "image_data_version": "1",
      "avoid_cache": false,
      "publisher_email": "",
      "publisher_website": "",
      "privacy_policy_website": "",
      "license_agreement_website": "",
      "stickers":
    * */

    public StickerPack(@NonNull UUID identifier,
                       @NonNull String name,
                       @NonNull String publisher,
                       @NonNull String originalTrayImageFile,
                       @NonNull String resizedTrayImageFile,
                       @NonNull String folderName,
                       @NonNull Integer imageDataVersion,
                       @NonNull Boolean avoidCache,
                       Boolean animatedStickerPack,
                       List<Sticker> stickerList) {
        this.identifier = identifier;
        this.name = name;
        this.publisher = publisher;
        this.originalTrayImageFile = originalTrayImageFile;
        this.resizedTrayImageFile = resizedTrayImageFile;
        this.folderName = folderName;
        this.imageDataVersion = imageDataVersion;
        this.avoidCache = avoidCache;
        this.animatedStickerPack = animatedStickerPack!=null && animatedStickerPack ? true : false;
        this.stickers = stickerList;
    }

    public StickerPack(UUID identifier,
                       String name,
                       String publisher,
                       String originalTrayImageFile,
                       String resizedTrayImageFile,
                       String folderName,
                       Integer imageDataVersion,
                       boolean avoidCache,
                       boolean animatedStickerPack,
                       String androidAppDownloadLinkInQuery,
                       String iosAppDownloadLinkInQuery) {
        this.identifier = identifier;
        this.name = name;
        this.publisher = publisher;
        this.originalTrayImageFile = originalTrayImageFile;
        this.resizedTrayImageFile = resizedTrayImageFile;
        this.folderName = folderName;
        this.imageDataVersion = imageDataVersion;
        this.avoidCache = avoidCache;
        this.animatedStickerPack = animatedStickerPack;
        this.androidPlayStoreLink = androidAppDownloadLinkInQuery;
        this.iosAppStoreLink = iosAppDownloadLinkInQuery;
    }

    public StickerPack(UUID identifier,
                       String name,
                       String publisher,
                       String originalTrayImageFile,
                       String resizedTrayImageFile,
                       String folderName,
                       Integer imageDataVersion,
                       boolean animatedStickerPack,
                       byte[] resizedTrayImageFileInBytes) {
        this.identifier = identifier;
        this.name = name;
        this.publisher = publisher;
        this.originalTrayImageFile = originalTrayImageFile;
        this.resizedTrayImageFile = resizedTrayImageFile;
        this.imageDataVersion = imageDataVersion;
        this.folderName = folderName;
        this.avoidCache = false;
        this.animatedStickerPack = animatedStickerPack;
        this.resizedTrayImageFileInBytes = resizedTrayImageFileInBytes;
    }

    public void setIsWhitelisted(boolean isWhitelisted) {
        this.isWhitelisted = isWhitelisted;
    }

    public boolean getIsWhitelisted() {
        return isWhitelisted;
    }

    private StickerPack(Parcel in) {
        identifier = UUID.fromString(in.readString());
        name = in.readString();
        publisher = in.readString();
        originalTrayImageFile = in.readString();
        resizedTrayImageFile = in.readString();
        folderName = in.readString();
        iosAppStoreLink = in.readString();
        stickers = in.createTypedArrayList(Sticker.CREATOR);
        totalSize = in.readLong();
        androidPlayStoreLink = in.readString();
        isWhitelisted = in.readByte() != 0;
        imageDataVersion = in.readInt();
        avoidCache = in.readByte() != 0;
        animatedStickerPack = in.readByte() != 0;
    }

    public static final Creator<StickerPack> CREATOR = new Creator<StickerPack>() {
        @Override
        public StickerPack createFromParcel(Parcel in) {
            return new StickerPack(in);
        }

        @Override
        public StickerPack[] newArray(int size) {
            return new StickerPack[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(identifier.toString());
        dest.writeString(name);
        dest.writeString(publisher);
        dest.writeString(originalTrayImageFile);
        dest.writeString(resizedTrayImageFile);
        dest.writeString(folderName);
        dest.writeString(iosAppStoreLink);
        dest.writeTypedList(stickers);
        dest.writeLong(totalSize);
        dest.writeString(androidPlayStoreLink);
        dest.writeByte((byte) (isWhitelisted ? 1 : 0));
        dest.writeInt(imageDataVersion);
        dest.writeByte((byte) (avoidCache ? 1 : 0));
        dest.writeByte((byte) (animatedStickerPack ? 1 : 0));
    }

    public void setStickers(List<Sticker> stickers) {
        this.stickers = stickers;
        totalSize = 0;
        for (Sticker sticker : stickers) {
            totalSize += sticker.getSize();
        }
    }

    public List<Sticker> getStickers() {
        if (stickers == null) {
            stickers = new ArrayList<>();
        }
        return stickers;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public UUID getIdentifier() {
        return identifier;
    }

    public String getName() {
        return name;
    }

    public String getPublisher() {
        return publisher;
    }

    public String getOriginalTrayImageFile() {
        return originalTrayImageFile;
    }

    public Integer getImageDataVersion() {
        return imageDataVersion;
    }

    public boolean isAvoidCache() {
        return avoidCache;
    }

    public boolean isAnimatedStickerPack() {
        return animatedStickerPack;
    }

    public String getIosAppStoreLink() {
        return iosAppStoreLink;
    }

    public String getAndroidPlayStoreLink() {
        return androidPlayStoreLink;
    }

    public String getFolderName() {
        return folderName;
    }

    public String getResizedTrayImageFile() {
        return resizedTrayImageFile;
    }

    public void setIdentifier(UUID identifier) {
        if (this.identifier == null) {
            this.identifier = identifier;
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public byte[] getResizedTrayImageFileInBytes() {
        return resizedTrayImageFileInBytes;
    }

    public void setResizedTrayImageFileInBytes(byte[] resizedTrayImageFileInBytes) {
        this.resizedTrayImageFileInBytes = resizedTrayImageFileInBytes;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        StickerPack that = (StickerPack) o;

        for (Sticker sticker : stickers) {
            if (that.getStickers().stream().noneMatch(s->s.equals(sticker))) {
                return false;
            }
        }
        return avoidCache == that.avoidCache &&
                animatedStickerPack == that.animatedStickerPack &&
                totalSize == that.totalSize &&
                isWhitelisted == that.isWhitelisted &&
                Objects.equals(identifier, that.identifier) &&
                Objects.equals(name, that.name) &&
                Objects.equals(publisher, that.publisher) &&
                Objects.equals(originalTrayImageFile, that.originalTrayImageFile) &&
                Objects.equals(resizedTrayImageFile, that.resizedTrayImageFile) &&
                Objects.equals(imageDataVersion, that.imageDataVersion) &&
                Objects.equals(folderName, that.folderName) &&
                (Objects.equals(iosAppStoreLink, that.iosAppStoreLink) || (Utils.isNothing(iosAppStoreLink) && Utils.isNothing(that.iosAppStoreLink))) &&
                (Objects.equals(androidPlayStoreLink, that.androidPlayStoreLink) || (Utils.isNothing(androidPlayStoreLink) && Utils.isNothing(that.androidPlayStoreLink)));
    }

}
