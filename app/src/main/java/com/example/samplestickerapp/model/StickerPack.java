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

import androidx.annotation.NonNull;

import java.util.List;

public class StickerPack implements Parcelable {
    private final String identifier;
    private final String name;
    private final String publisher;
    private final String trayImageFile;
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
                       @NonNull String trayImageFile,
                       @NonNull String folder,
                       @NonNull Integer imageDataVersion,
                       @NonNull Boolean avoidCache,
                       String publisherEmail,
                       String publisherWebsite,
                       String privacyPolicyWebsite,
                       String licenseAgreementWebsite,
                       Boolean animatedStickerPack,
                       List<Sticker> stickerList) {
        this.identifier = identifier+"";
        this.name = name;
        this.publisher = publisher;
        this.trayImageFile = trayImageFile;
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

    public StickerPack(String identifier,
                       String name,
                       String publisher,
                       String trayImageFile,
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
        this.trayImageFile = trayImageFile;
        this.folder = folder;
        this.publisherEmail = publisherEmail;
        this.publisherWebsite = publisherWebsite;
        this.privacyPolicyWebsite = privacyPolicyWebsite;
        this.licenseAgreementWebsite = licenseAgreementWebsite;
        this.imageDataVersion = imageDataVersion;
        this.avoidCache = avoidCache;
        this.animatedStickerPack = animatedStickerPack;
    }

    public StickerPack(String identifier,
                       String name,
                       String publisher,
                       String trayImageFile,
                       String folder,
                       String imageDataVersion,
                       boolean animatedStickerPack) {
        this.identifier = identifier;
        this.name = name;
        this.publisher = publisher;
        this.trayImageFile = trayImageFile;
        this.imageDataVersion = imageDataVersion;
        this.folder = folder;
        this.avoidCache = false;
        this.publisherEmail = "";
        this.publisherWebsite = "";
        this.privacyPolicyWebsite = "";
        this.licenseAgreementWebsite = "";
        this.animatedStickerPack = animatedStickerPack;
    }

    public void setIsWhitelisted(boolean isWhitelisted) {
        this.isWhitelisted = isWhitelisted;
    }

    public boolean getIsWhitelisted() {
        return isWhitelisted;
    }

    private StickerPack(Parcel in) {
        identifier = in.readString();
        name = in.readString();
        publisher = in.readString();
        trayImageFile = in.readString();
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
        dest.writeString(identifier);
        dest.writeString(name);
        dest.writeString(publisher);
        dest.writeString(trayImageFile);
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

    public String getIdentifier() {
        return identifier;
    }

    public String getName() {
        return name;
    }

    public String getPublisher() {
        return publisher;
    }

    public String getTrayImageFile() {
        return trayImageFile;
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
}
