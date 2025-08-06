/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.miguelteles.beststickerapp.validator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.webkit.URLUtil;

import androidx.annotation.NonNull;

import io.github.miguelteles.beststickerapp.domain.entity.Sticker;
import io.github.miguelteles.beststickerapp.domain.entity.StickerPack;
import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.exception.StickerFolderException;
import io.github.miguelteles.beststickerapp.exception.enums.StickerExceptionEnum;
import io.github.miguelteles.beststickerapp.utils.Utils;
import io.github.miguelteles.beststickerapp.view.EntryActivity;

import com.facebook.animated.webp.WebPImage;
import com.facebook.imagepipeline.common.ImageDecodeOptions;

import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class StickerPackValidator {

    private static StickerPackValidator instance;

    private static final int STATIC_STICKER_FILE_LIMIT_KB = 100;
    private static final int ANIMATED_STICKER_FILE_LIMIT_KB = 500;
    public static final int EMOJI_MAX_LIMIT = 3;
    private static final int EMOJI_MIN_LIMIT = 1;
    private static final int IMAGE_HEIGHT = 512;
    private static final int IMAGE_WIDTH = 512;
    private static final int STICKER_SIZE_MIN = 3;
    private static final int STICKER_SIZE_MAX = 30;
    private static final int CHAR_COUNT_MAX = 128;
    private static final long KB_IN_BYTES = 1024;
    private static final int TRAY_IMAGE_FILE_SIZE_MAX_KB = 50;
    private static final int TRAY_IMAGE_DIMENSION_MIN = 24;
    private static final int TRAY_IMAGE_DIMENSION_MAX = 512;
    private static final int ANIMATED_STICKER_FRAME_DURATION_MIN = 8;
    private static final int ANIMATED_STICKER_TOTAL_DURATION_MAX = 10 * 1000; //ms
    private static final String PLAY_STORE_DOMAIN = "play.google.com";
    private static final String APPLE_STORE_DOMAIN = "itunes.apple.com";

    private StickerPackValidator() {
    }

    public static StickerPackValidator getInstance() throws StickerException {
        if (instance == null) {
            instance = new StickerPackValidator();
        }
        return instance;
    }

    public void verifyStickerPacks(@NonNull Context context,
                                   @NotNull List<StickerPack> stickerPackList) throws StickerException {
        HashSet<String> identifierSet = new HashSet<>();
        for (StickerPack stickerPack : stickerPackList) {
            if (identifierSet.contains(stickerPack.getIdentifier().toString())) {
                throw new IllegalStateException("sticker pack identifiers should be unique, there are more than one pack with identifier:" + stickerPack.getIdentifier());
            } else {
                identifierSet.add(stickerPack.getIdentifier().toString());
            }
        }
        for (StickerPack stickerPack : stickerPackList) {
            this.verifyStickerPackValidity(stickerPack);
        }
    }

    public void verifyCreatedStickerPackValidity(StickerPack stickerPack) throws StickerException {
        try {
            validateStickerPack(stickerPack);
        } catch (Exception e) {
            throw new StickerException(e, StickerExceptionEnum.ISP, "Error validating sticker pack");
        }
    }

    /**
     * Checks whether a sticker pack contains valid data
     */
    public void verifyStickerPackValidity(@NonNull StickerPack stickerPack) throws StickerException {
        try {
            if (TextUtils.isEmpty(stickerPack.getIdentifier().toString())) {
                throw new IllegalStateException("sticker pack identifier is empty");
            }
            if (stickerPack.getIdentifier().toString().length() > CHAR_COUNT_MAX) {
                throw new IllegalStateException("sticker pack identifier cannot exceed " + CHAR_COUNT_MAX + " characters");
            }
            checkStringValidity(stickerPack.getIdentifier().toString());
            validateStickerPack(stickerPack);
            final List<Sticker> stickers = stickerPack.getStickers();
            for (final Sticker sticker : stickers) {
                validateSticker(stickerPack.getIdentifier(), sticker, stickerPack.isAnimatedStickerPack());
            }
        } catch (IllegalStateException e) {
            throw new StickerException(e, StickerExceptionEnum.ISP, e.getMessage());
        } catch (Exception e) {
            throw new StickerException(e, StickerExceptionEnum.ISP, "Error validating sticker pack");
        }
    }

    private void validateStickerPack(@NonNull StickerPack stickerPack) {
        if (TextUtils.isEmpty(stickerPack.getPublisher())) {
            throw new IllegalStateException("sticker pack publisher is empty, sticker pack identifier: " + stickerPack.getIdentifier());
        }
        if (stickerPack.getPublisher().length() > CHAR_COUNT_MAX) {
            throw new IllegalStateException("sticker pack publisher cannot exceed " + CHAR_COUNT_MAX + " characters, sticker pack identifier: " + stickerPack.getIdentifier());
        }
        if (TextUtils.isEmpty(stickerPack.getName())) {
            throw new IllegalStateException("sticker pack name is empty, sticker pack identifier: " + stickerPack.getIdentifier());
        }
        if (stickerPack.getName().length() > CHAR_COUNT_MAX) {
            throw new IllegalStateException("sticker pack name cannot exceed " + CHAR_COUNT_MAX + " characters, sticker pack identifier: " + stickerPack.getIdentifier());
        }
        if (TextUtils.isEmpty(stickerPack.getOriginalTrayImageFile())) {
            throw new IllegalStateException("sticker pack tray id is empty, sticker pack identifier:" + stickerPack.getIdentifier());
        }
        if (stickerPack.getResizedTrayImageFileInBytes().length > TRAY_IMAGE_FILE_SIZE_MAX_KB * KB_IN_BYTES) {
            throw new IllegalStateException("tray image should be less than " + TRAY_IMAGE_FILE_SIZE_MAX_KB + " KB, tray image file: " + stickerPack.getOriginalTrayImageFile());
        }
        try {
            Bitmap bitmap = BitmapFactory.decodeByteArray(stickerPack.getResizedTrayImageFileInBytes(), 0, stickerPack.getResizedTrayImageFileInBytes().length);
            if (bitmap.getHeight() > TRAY_IMAGE_DIMENSION_MAX || bitmap.getHeight() < TRAY_IMAGE_DIMENSION_MIN) {
                throw new IllegalStateException("tray image height should between " + TRAY_IMAGE_DIMENSION_MIN + " and " + TRAY_IMAGE_DIMENSION_MAX + " pixels, current tray image height is " + bitmap.getHeight() + ", tray image file: " + stickerPack.getOriginalTrayImageFile());
            }
            if (bitmap.getWidth() > TRAY_IMAGE_DIMENSION_MAX || bitmap.getWidth() < TRAY_IMAGE_DIMENSION_MIN) {
                throw new IllegalStateException("tray image width should be between " + TRAY_IMAGE_DIMENSION_MIN + " and " + TRAY_IMAGE_DIMENSION_MAX + " pixels, current tray image width is " + bitmap.getWidth() + ", tray image file: " + stickerPack.getOriginalTrayImageFile());
            }
        } catch (Exception e) {
            throw new IllegalStateException("Cannot open tray image, " + stickerPack.getOriginalTrayImageFile(), e);
        }
    }

    public void validateSticker(@NonNull final UUID packIdentifier,
                                @NonNull final Sticker sticker,
                                final boolean animatedStickerPack) throws IllegalStateException, StickerFolderException {
        if (TextUtils.isEmpty(sticker.getStickerImageFile())) {
            throw new IllegalStateException("no file path for sticker, sticker pack identifier:" + packIdentifier);
        }
        validateStickerFile(packIdentifier, sticker.getStickerImageFile(), animatedStickerPack, sticker.getStickerImageFileInBytes());
    }

    private void validateStickerFile(@NonNull UUID packIdentifier,
                                     @NonNull final String fileName,
                                     final boolean animatedStickerPack,
                                     final byte[] stickerImageFileInBytes) throws IllegalStateException {
        try {
            if (!animatedStickerPack && stickerImageFileInBytes.length > STATIC_STICKER_FILE_LIMIT_KB * KB_IN_BYTES) {
                throw new IllegalStateException("static sticker should be less than " + STATIC_STICKER_FILE_LIMIT_KB + "KB, current file is " + stickerImageFileInBytes.length / KB_IN_BYTES + " KB, sticker pack identifier: " + packIdentifier + ", filename: " + fileName);
            }
            if (animatedStickerPack && stickerImageFileInBytes.length > ANIMATED_STICKER_FILE_LIMIT_KB * KB_IN_BYTES) {
                throw new IllegalStateException("animated sticker should be less than " + ANIMATED_STICKER_FILE_LIMIT_KB + "KB, current file is " + stickerImageFileInBytes.length / KB_IN_BYTES + " KB, sticker pack identifier: " + packIdentifier + ", filename: " + fileName);
            }
            try {
                final WebPImage webPImage = WebPImage.createFromByteArray(stickerImageFileInBytes, ImageDecodeOptions.defaults());
                if (webPImage.getHeight() != IMAGE_HEIGHT) {
                    throw new IllegalStateException("sticker height should be " + IMAGE_HEIGHT + ", current height is " + webPImage.getHeight() + ", sticker pack identifier: " + packIdentifier + ", filename: " + fileName);
                }
                if (webPImage.getWidth() != IMAGE_WIDTH) {
                    throw new IllegalStateException("sticker width should be " + IMAGE_WIDTH + ", current width is " + webPImage.getWidth() + ", sticker pack identifier: " + packIdentifier + ", filename: " + fileName);
                }
                if (animatedStickerPack) {
                    if (webPImage.getFrameCount() <= 1) {
                        throw new IllegalStateException("this pack is marked as animated sticker pack, all stickers should animate, sticker pack identifier: " + packIdentifier + ", filename: " + fileName);
                    }
                    checkFrameDurationsForAnimatedSticker(webPImage.getFrameDurations(), packIdentifier, fileName);
                    if (webPImage.getDuration() > ANIMATED_STICKER_TOTAL_DURATION_MAX) {
                        throw new IllegalStateException("sticker animation max duration is: " + ANIMATED_STICKER_TOTAL_DURATION_MAX + " ms, current duration is: " + webPImage.getDuration() + " ms, sticker pack identifier: " + packIdentifier + ", filename: " + fileName);
                    }
                } else if (webPImage.getFrameCount() > 1) {
                    throw new IllegalStateException("this pack is not marked as animated sticker pack, all stickers should be static stickers, sticker pack identifier: " + packIdentifier + ", filename: " + fileName);
                }
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException("Error parsing webp image, sticker pack identifier: " + packIdentifier + ", filename: " + fileName, e);
            }
        } catch (IllegalStateException ex) {
            if (EntryActivity.SAFE_MODE) {
                System.err.println(ex.getMessage());
            } else {
                throw ex;
            }
        }
    }

    private void checkFrameDurationsForAnimatedSticker(@NonNull final int[] frameDurations, @NonNull final UUID identifier, @NonNull final String fileName) {
        for (int frameDuration : frameDurations) {
            if (frameDuration < ANIMATED_STICKER_FRAME_DURATION_MIN) {
                throw new IllegalStateException("animated sticker frame duration limit is " + ANIMATED_STICKER_FRAME_DURATION_MIN + ", sticker pack identifier: " + identifier + ", filename: " + fileName);
            }
        }
    }

    private void checkStringValidity(@NonNull String string) {
        String pattern = "[\\w-.,'\\s]+"; // [a-zA-Z0-9_-.' ]
        if (!string.matches(pattern)) {
            throw new IllegalStateException(string + " contains invalid characters, allowed characters are a to z, A to Z, _ , ' - . and space character");
        }
        if (string.contains("..")) {
            throw new IllegalStateException(string + " cannot contain ..");
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isValidWebsiteUrl(String websiteUrl) throws IllegalStateException {
        try {
            new URL(websiteUrl);
        } catch (MalformedURLException e) {
            Log.e("StickerPackValidator", "url: " + websiteUrl + " is malformed");
            throw new IllegalStateException("url: " + websiteUrl + " is malformed", e);
        }
        return URLUtil.isHttpUrl(websiteUrl) || URLUtil.isHttpsUrl(websiteUrl);

    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isURLInCorrectDomain(String urlString, String domain) throws IllegalStateException {
        try {
            URL url = new URL(urlString);
            if (domain.equals(url.getHost())) {
                return true;
            }
        } catch (MalformedURLException e) {
            Log.e("StickerPackValidator", "url: " + urlString + " is malformed");
            throw new IllegalStateException("url: " + urlString + " is malformed");
        }
        return false;
    }
}
