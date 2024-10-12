/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.example.samplestickerapp.view;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static com.example.samplestickerapp.view.StickerContentProvider.ANIMATED_STICKER_PACK;
import static com.example.samplestickerapp.view.StickerContentProvider.AVOID_CACHE;
import static com.example.samplestickerapp.view.StickerContentProvider.FOLDER;
import static com.example.samplestickerapp.view.StickerContentProvider.IDENTIFIER;
import static com.example.samplestickerapp.view.StickerContentProvider.IMAGE_DATA_VERSION;
import static com.example.samplestickerapp.view.StickerContentProvider.LICENSE_AGREENMENT_WEBSITE;
import static com.example.samplestickerapp.view.StickerContentProvider.NAME;
import static com.example.samplestickerapp.view.StickerContentProvider.PRIVACY_POLICY_WEBSITE;
import static com.example.samplestickerapp.view.StickerContentProvider.PUBLISHER;
import static com.example.samplestickerapp.view.StickerContentProvider.PUBLISHER_EMAIL;
import static com.example.samplestickerapp.view.StickerContentProvider.PUBLISHER_WEBSITE;
import static com.example.samplestickerapp.view.StickerContentProvider.RESIZED_TRAY_IMAGE_FILE;
import static com.example.samplestickerapp.view.StickerContentProvider.STICKERS;
import static com.example.samplestickerapp.view.StickerContentProvider.STICKER_FILE_EMOJI_IN_QUERY;
import static com.example.samplestickerapp.view.StickerContentProvider.STICKER_FILE_NAME_IN_QUERY;
import static com.example.samplestickerapp.view.StickerContentProvider.ORIGINAL_TRAY_IMAGE_FILE;

import com.example.samplestickerapp.BuildConfig;
import com.example.samplestickerapp.model.Sticker;
import com.example.samplestickerapp.model.StickerPack;

public class StickerPackLoader {

    /**
     * Get the list of sticker packs for the sticker content provider
     */
    @NonNull
    static ArrayList<StickerPack> fetchStickerPacks(Context context) throws IllegalStateException {
        final Cursor cursor = context.getContentResolver().query(StickerContentProvider.AUTHORITY_URI, null, null, null, null);
        if (cursor == null) {
            throw new IllegalStateException("could not fetch from content provider, " + BuildConfig.CONTENT_PROVIDER_AUTHORITY);
        }
        HashSet<String> identifierSet = new HashSet<>();
        final ArrayList<StickerPack> stickerPackList = fetchFromContentProvider(cursor);
        for (StickerPack stickerPack : stickerPackList) {
            if (identifierSet.contains(stickerPack.getIdentifier())) {
                throw new IllegalStateException("sticker pack identifiers should be unique, there are more than one pack with identifier:" + stickerPack.getIdentifier());
            } else {
                identifierSet.add(stickerPack.getIdentifier().toString());
            }
        }
        for (StickerPack stickerPack : stickerPackList) {
            final List<Sticker> stickers = getStickersForPack(context, stickerPack);
            stickerPack.setStickers(stickers);
            StickerPackValidator.verifyStickerPackValidity(context, stickerPack);
        }
        return stickerPackList;
    }

    @NonNull
    private static List<Sticker> getStickersForPack(Context context, StickerPack stickerPack) {
        final List<Sticker> stickers = fetchFromContentProviderForStickers(stickerPack.getFolder(), context.getContentResolver());
        for (Sticker sticker : stickers) {
            final byte[] bytes;
            try {
                bytes = fetchStickerAsset(stickerPack.getIdentifier().toString(), sticker.getImageFileName(), context.getContentResolver());
                if (bytes.length <= 0) {
                    throw new IllegalStateException("Asset file is empty, pack: " + stickerPack.getName() + ", sticker: " + sticker.getImageFileName());
                }
                sticker.setSize(bytes.length);
            } catch (IOException | IllegalArgumentException e) {
                throw new IllegalStateException("Asset file doesn't exist. pack: " + stickerPack.getName() + ", sticker: " + sticker.getImageFileName(), e);
            }
        }
        return stickers;
    }


    @NonNull
    private static ArrayList<StickerPack> fetchFromContentProvider(Cursor cursor) {
        ArrayList<StickerPack> stickerPackList = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            final int identifier = cursor.getInt(cursor.getColumnIndexOrThrow(IDENTIFIER));
            final String name = cursor.getString(cursor.getColumnIndexOrThrow(NAME));
            final String publisher = cursor.getString(cursor.getColumnIndexOrThrow(PUBLISHER));
            final String originalTrayImage = cursor.getString(cursor.getColumnIndexOrThrow(ORIGINAL_TRAY_IMAGE_FILE));
            final String resizedTrayImage = cursor.getString(cursor.getColumnIndex(RESIZED_TRAY_IMAGE_FILE));
            final String folder = cursor.getString(cursor.getColumnIndexOrThrow(FOLDER));
            final String publisherEmail = cursor.getString(cursor.getColumnIndexOrThrow(PUBLISHER_EMAIL));
            final String publisherWebsite = cursor.getString(cursor.getColumnIndexOrThrow(PUBLISHER_WEBSITE));
            final String privacyPolicyWebsite = cursor.getString(cursor.getColumnIndexOrThrow(PRIVACY_POLICY_WEBSITE));
            final String licenseAgreementWebsite = cursor.getString(cursor.getColumnIndexOrThrow(LICENSE_AGREENMENT_WEBSITE));
            final String imageDataVersion = cursor.getString(cursor.getColumnIndexOrThrow(IMAGE_DATA_VERSION));
            final boolean avoidCache = cursor.getShort(cursor.getColumnIndexOrThrow(AVOID_CACHE)) > 0;
            final boolean animatedStickerPack = cursor.getShort(cursor.getColumnIndexOrThrow(ANIMATED_STICKER_PACK)) > 0;
            final StickerPack stickerPack = new StickerPack(identifier,
                    name,
                    publisher,
                    originalTrayImage,
                    resizedTrayImage,
                    folder,
                    publisherEmail,
                    publisherWebsite,
                    privacyPolicyWebsite,
                    licenseAgreementWebsite,
                    imageDataVersion,
                    avoidCache,
                    animatedStickerPack);
            stickerPackList.add(stickerPack);
            cursor.moveToNext();
        }

                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   return stickerPackList;
    }

    @NonNull
    private static List<Sticker> fetchFromContentProviderForStickers(String folder, ContentResolver contentResolver) {
        Uri uri = getStickerListUri(folder);

        final String[] projection = {STICKER_FILE_NAME_IN_QUERY, STICKER_FILE_EMOJI_IN_QUERY};
        final Cursor cursor = contentResolver.query(uri, projection, null, null, null);
        List<Sticker> stickers = new ArrayList<>();
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                final String name = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_FILE_NAME_IN_QUERY));
                final String emojisConcatenated = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_FILE_EMOJI_IN_QUERY));
                List<String> emojis = new ArrayList<>(StickerPackValidator.EMOJI_MAX_LIMIT);
                if (!TextUtils.isEmpty(emojisConcatenated)) {
                    emojis = Arrays.asList(emojisConcatenated.split(","));
                }
                stickers.add(new Sticker(name, emojis, null));
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
        return stickers;
    }

    /**
     * Busca um asset da pasta (imagem da figurinha ou da capa do sticker pack)
     * **/
    static byte[] fetchStickerAsset(@NonNull final String identifier, @NonNull final String stickerImageFileName, ContentResolver contentResolver) throws IOException {
        //o contentResolver.openInputStream vai pro método openAssetFile do contentProvider
        try (final InputStream inputStream = contentResolver.openInputStream(getStickerAssetUri(identifier, stickerImageFileName));
             final ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            if (inputStream == null) {
                throw new IOException("cannot read sticker asset id: " + identifier + "; name: " + stickerImageFileName);
            }
            int read;
            byte[] data = new byte[16384];

            while ((read = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, read);
            }
            return buffer.toByteArray();
        } catch (IOException ex) {
            throw ex;
        }
    }

    private static Uri getStickerListUri(String identifier) {
        return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(BuildConfig.CONTENT_PROVIDER_AUTHORITY).appendPath(STICKERS).appendPath(identifier).build();
    }

    static Uri getStickerAssetUri(String identifier, String imageName) {
        return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(BuildConfig.CONTENT_PROVIDER_AUTHORITY).appendPath(StickerContentProvider.STICKERS_ASSET).appendPath(identifier).appendPath(imageName).build();
    }

    public static Uri getStickerPackInsertUri() {
        return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(BuildConfig.CONTENT_PROVIDER_AUTHORITY).appendPath(StickerContentProvider.METHODS_ADD).appendPath(StickerContentProvider.PACK).build();
    }

    public static Uri getStickerPackUpdateUri() {
        return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(BuildConfig.CONTENT_PROVIDER_AUTHORITY).appendPath(StickerContentProvider.METHODS_UPDATE).appendPath(StickerContentProvider.PACK).build();
    }

    public static Uri getStickerPackDeleteUri() {
        return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(BuildConfig.CONTENT_PROVIDER_AUTHORITY).appendPath(StickerContentProvider.METHODS_DELETE).appendPath(StickerContentProvider.PACK).build();
    }
}
