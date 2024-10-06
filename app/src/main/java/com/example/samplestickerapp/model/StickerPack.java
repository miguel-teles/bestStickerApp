/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.example.samplestickerapp.model;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class StickerPack implements Parcelable {

    public static final String NM_TABELA = "packs";
    private static final String IDENTIFIER = "identifier";
    private static final String NAME = "name";
    private static final String PUBLISHER = "publisher";
    private static final String ORIGINAL_TRAY_IMAGE_FILE = "originalTrayImageFile";
    private static final String RESIZED_TRAY_IMAGE_FILE = "resizedTrayImageFile";
    private static final String PUBLISHER_EMAIL = "publisherEmail";
    private static final String PUBLISHER_WEBSITE = "publisherWebsite";
    private static final String PRIVACY_POLICY_WEBSITE = "privacyPolicyWebsite";
    private static final String LICENSE_AGREEMENT_WEBSITE = "licenseAgreementWebsite";
    private static final String IMAGE_DATA_VERSION = "imageDataVersion";
    private static final String FOLDER = "folder";
    private static final String AVOID_CACHE = "avoidCache";
    private static final String ANIMATED_STICKER_PACK = "animatedStickerPack";

    private Integer identifier;
    private final String name;
    private final String publisher;
    private final String originalTrayImageFile;
    private final String resizedTrayImageFile;
    private final String publisherEmail;
    private final String publisherWebsite;
    private final String privacyPolicyWebsite;
    private final String licenseAgreementWebsite;
    private final String imageDataVersion;
    private final String folder;
    private final boolean avoidCache;
    private final boolean animatedStickerPack;
    private String iosAppStoreLink;
    private List<Sticker> stickers;
    private long totalSize;
    private String androidPlayStoreLink;
    private boolean isWhitelisted;


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

    public StickerPack(@NonNull Integer identifier,
                       @NonNull String name,
                       @NonNull String publisher,
                       @NonNull String originalTrayImageFile,
                       @NonNull String resizedTrayImageFile,
                       @NonNull String folder,
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
        this.folder = folder;
        this.imageDataVersion = imageDataVersion+"";
        this.avoidCache = avoidCache;
        this.publisherEmail = publisherEmail;
        this.publisherWebsite = publisherWebsite;
        this.licenseAgreementWebsite = licenseAgreementWebsite;
        this.privacyPolicyWebsite = privacyPolicyWebsite;
        this.animatedStickerPack = animatedStickerPack!=null && animatedStickerPack ? true : false;
        this.stickers = stickerList;
    }

    public StickerPack(Integer identifier,
                       String name,
                       String publisher,
                       String originalTrayImageFile,
                       String resizedTrayImageFile,
                       String folder,
                       String publisherEmail,
                       String publisherWebsite,
                       String privacyPolicyWebsite,
                       String licenseAgreementWebsite,
                       String imageDataVersion,
                       boolean avoidCache,
                       boolean animatedStickerPack) {
        this.identifier = identifier;
        this.name = name;
        this.publisher = publisher;
        this.originalTrayImageFile = originalTrayImageFile;
        this.resizedTrayImageFile = resizedTrayImageFile;
        this.folder = folder;
        this.publisherEmail = publisherEmail;
        this.publisherWebsite = publisherWebsite;
        this.privacyPolicyWebsite = privacyPolicyWebsite;
        this.licenseAgreementWebsite = licenseAgreementWebsite;
        this.imageDataVersion = imageDataVersion;
        this.avoidCache = avoidCache;
        this.animatedStickerPack = animatedStickerPack;
    }

    public StickerPack(Integer identifier,
                       String name,
                       String publisher,
                       String originalTrayImageFile,
                       String resizedTrayImageFile,
                       String folder,
                       String imageDataVersion,
                       boolean animatedStickerPack) {
        this.identifier = identifier;
        this.name = name;
        this.publisher = publisher;
        this.originalTrayImageFile = originalTrayImageFile;
        this.resizedTrayImageFile = resizedTrayImageFile;
        this.imageDataVersion = imageDataVersion;
        this.folder = folder;
        this.avoidCache = false;
        this.publisherEmail = "";
        this.publisherWebsite = "";
        this.privacyPolicyWebsite = "";
        this.licenseAgreementWebsite = "";
        this.animatedStickerPack = animatedStickerPack;
    }

    public static StickerPack fromContentValues(ContentValues values) {
        return new StickerPack(values.getAsInteger(IDENTIFIER),
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
        contentValues.put(IDENTIFIER, this.getIdentifier());
        contentValues.put(NAME, this.getName());
        contentValues.put(PUBLISHER, this.getPublisher());
        contentValues.put(ORIGINAL_TRAY_IMAGE_FILE, this.getOriginalTrayImageFile());
        contentValues.put(RESIZED_TRAY_IMAGE_FILE, this.getResizedTrayImageFile());
        contentValues.put(IMAGE_DATA_VERSION, this.getImageDataVersion());
        contentValues.put(FOLDER, this.getFolder());
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
        identifier = in.readInt();
        name = in.readString();
        publisher = in.readString();
        originalTrayImageFile = in.readString();
        resizedTrayImageFile = in.readString();
        folder = in.readString();
        publisherEmail = in.readString();
        publisherWebsite = in.readString();
        privacyPolicyWebsite = in.readString();
        licenseAgreementWebsite = in.readString();
        iosAppStoreLink = in.readString();
        stickers = in.createTypedArrayList(Sticker.CREATOR);
        totalSize = in.readLong();
        androidPlayStoreLink = in.readString();
        isWhitelisted = in.readByte() != 0;
        imageDataVersion = in.readString();
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

    public void setStickers(List<Sticker> stickers) {
        this.stickers = stickers;
        totalSize = 0;
        for (Sticker sticker : stickers) {
            totalSize += sticker.getSize();
        }
    }

    public void setAndroidPlayStoreLink(String androidPlayStoreLink) {
        this.androidPlayStoreLink = androidPlayStoreLink;
    }

    public void setIosAppStoreLink(String iosAppStoreLink) {
        this.iosAppStoreLink = iosAppStoreLink;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(identifier);
        dest.writeString(name);
        dest.writeString(publisher);
        dest.writeString(originalTrayImageFile);
        dest.writeString(resizedTrayImageFile);
        dest.writeString(folder);
        dest.writeString(publisherEmail);
        dest.writeString(publisherWebsite);
        dest.writeString(privacyPolicyWebsite);
        dest.writeString(licenseAgreementWebsite);
        dest.writeString(iosAppStoreLink);
        dest.writeTypedList(stickers);
        dest.writeLong(totalSize);
        dest.writeString(androidPlayStoreLink);
        dest.writeByte((byte) (isWhitelisted ? 1 : 0));
        dest.writeString(imageDataVersion);
        dest.writeByte((byte) (avoidCache ? 1 : 0));
        dest.writeByte((byte) (animatedStickerPack ? 1 : 0));
    }

    public Integer getIdentifier() {
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

    public String getImageDataVersion() {
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

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public String getAndroidPlayStoreLink() {
        return androidPlayStoreLink;
    }

    public boolean isWhitelisted() {
        return isWhitelisted;
    }

    public void setWhitelisted(boolean whitelisted) {
        isWhitelisted = whitelisted;
    }

    public String getFolder() {
        return folder;
    }

    public String getResizedTrayImageFile() {
        return resizedTrayImageFile;
    }

    public void setIdentifier(Integer identifier) {
        if (this.identifier == null) {
            this.identifier = identifier;
        }
    }
}
