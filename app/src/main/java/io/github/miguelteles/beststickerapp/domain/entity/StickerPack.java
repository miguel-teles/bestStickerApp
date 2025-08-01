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
    public static final String PUBLISHER_EMAIL = "publisherEmail";
    public static final String PUBLISHER_WEBSITE = "publisherWebsite";
    public static final String PRIVACY_POLICY_WEBSITE = "privacyPolicyWebsite";
    public static final String LICENSE_AGREEMENT_WEBSITE = "licenseAgreementWebsite";
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
    private final String publisherEmail;
    private final String publisherWebsite;
    private final String privacyPolicyWebsite;
    private final String licenseAgreementWebsite;
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
                       String publisherEmail,
                       String publisherWebsite,
                       String privacyPolicyWebsite,
                       String licenseAgreementWebsite,
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
        this.publisherEmail = publisherEmail;
        this.publisherWebsite = publisherWebsite;
        this.licenseAgreementWebsite = licenseAgreementWebsite;
        this.privacyPolicyWebsite = privacyPolicyWebsite;
        this.animatedStickerPack = animatedStickerPack!=null && animatedStickerPack ? true : false;
        this.stickers = stickerList;
    }

    public StickerPack(UUID identifier,
                       String name,
                       String publisher,
                       String originalTrayImageFile,
                       String resizedTrayImageFile,
                       String folderName,
                       String publisherEmail,
                       String publisherWebsite,
                       String privacyPolicyWebsite,
                       String licenseAgreementWebsite,
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
        this.publisherEmail = publisherEmail;
        this.publisherWebsite = publisherWebsite;
        this.privacyPolicyWebsite = privacyPolicyWebsite;
        this.licenseAgreementWebsite = licenseAgreementWebsite;
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
        this.publisherEmail = "";
        this.publisherWebsite = "";
        this.privacyPolicyWebsite = "";
        this.licenseAgreementWebsite = "";
        this.animatedStickerPack = animatedStickerPack;
        this.resizedTrayImageFileInBytes = resizedTrayImageFileInBytes;
    }

    public static StickerPack fromContentValues(ContentValues values) {
        return new StickerPack(UUID.fromString(values.getAsString(IDENTIFIER)),
                values.getAsString(NAME),
                values.getAsString(PUBLISHER),
                values.getAsString(ORIGINAL_TRAY_IMAGE_FILE),
                values.getAsString(RESIZED_TRAY_IMAGE_FILE),
                values.getAsString(FOLDER),
                values.getAsInteger(IMAGE_DATA_VERSION),
                values.getAsBoolean(AVOID_CACHE),
                values.getAsString(PUBLISHER_EMAIL),
                values.getAsString(PUBLISHER_WEBSITE),
                values.getAsString(PRIVACY_POLICY_WEBSITE),
                values.getAsString(LICENSE_AGREEMENT_WEBSITE),
                values.getAsBoolean(ANIMATED_STICKER_PACK),
                null);
    }

    public ContentValues toContentValues() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(IDENTIFIER, this.getIdentifier().toString());
        contentValues.put(NAME, this.getName());
        contentValues.put(PUBLISHER, this.getPublisher());
        contentValues.put(ORIGINAL_TRAY_IMAGE_FILE, this.getOriginalTrayImageFile());
        contentValues.put(RESIZED_TRAY_IMAGE_FILE, this.getResizedTrayImageFile());
        contentValues.put(IMAGE_DATA_VERSION, this.getImageDataVersion());
        contentValues.put(FOLDER, this.getFolderName());
        contentValues.put(AVOID_CACHE, this.isAvoidCache());
        contentValues.put(PUBLISHER_EMAIL, this.getPublisherEmail());
        contentValues.put(PUBLISHER_WEBSITE, this.getPublisherWebsite());
        contentValues.put(PRIVACY_POLICY_WEBSITE, this.getPrivacyPolicyWebsite());
        contentValues.put(LICENSE_AGREEMENT_WEBSITE, this.getLicenseAgreementWebsite());
        contentValues.put(ANIMATED_STICKER_PACK, this.isAnimatedStickerPack());
        return contentValues;
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
        publisherEmail = in.readString();
        publisherWebsite = in.readString();
        privacyPolicyWebsite = in.readString();
        licenseAgreementWebsite = in.readString();
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
        dest.writeString(publisherEmail);
        dest.writeString(publisherWebsite);
        dest.writeString(privacyPolicyWebsite);
        dest.writeString(licenseAgreementWebsite);
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

    public String getPublisherEmail() {
        return publisherEmail;
    }

    public String getPublisherWebsite() {
        return publisherWebsite;
    }

    public String getPrivacyPolicyWebsite() {
        return privacyPolicyWebsite;
    }

    public String getLicenseAgreementWebsite() {
        return licenseAgreementWebsite;
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
                Objects.equals(publisherEmail, that.publisherEmail) &&
                Objects.equals(publisherWebsite, that.publisherWebsite) &&
                Objects.equals(privacyPolicyWebsite, that.privacyPolicyWebsite) &&
                Objects.equals(licenseAgreementWebsite, that.licenseAgreementWebsite) &&
                Objects.equals(imageDataVersion, that.imageDataVersion) &&
                Objects.equals(folderName, that.folderName) &&
                (Objects.equals(iosAppStoreLink, that.iosAppStoreLink) || (Utils.isNothing(iosAppStoreLink) && Utils.isNothing(that.iosAppStoreLink))) &&
                (Objects.equals(androidPlayStoreLink, that.androidPlayStoreLink) || (Utils.isNothing(androidPlayStoreLink) && Utils.isNothing(that.androidPlayStoreLink)));
    }

}
