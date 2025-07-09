/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.miguelteles.beststickerapp.repository.contentProvider;

import static io.github.miguelteles.beststickerapp.repository.contentProvider.StickerContentProvider.ANIMATED_STICKER_PACK;
import static io.github.miguelteles.beststickerapp.repository.contentProvider.StickerContentProvider.AVOID_CACHE;
import static io.github.miguelteles.beststickerapp.repository.contentProvider.StickerContentProvider.FOLDER;
import static io.github.miguelteles.beststickerapp.repository.contentProvider.StickerContentProvider.IMAGE_DATA_VERSION;
import static io.github.miguelteles.beststickerapp.repository.contentProvider.StickerContentProvider.LICENSE_AGREEMENT_WEBSITE;
import static io.github.miguelteles.beststickerapp.repository.contentProvider.StickerContentProvider.PRIVACY_POLICY_WEBSITE;
import static io.github.miguelteles.beststickerapp.repository.contentProvider.StickerContentProvider.PUBLISHER_EMAIL;
import static io.github.miguelteles.beststickerapp.repository.contentProvider.StickerContentProvider.PUBLISHER_WEBSITE;
import static io.github.miguelteles.beststickerapp.repository.contentProvider.StickerContentProvider.STICKER_FILE_NAME_IN_QUERY;
import static io.github.miguelteles.beststickerapp.repository.contentProvider.StickerContentProvider.STICKER_IDENTIFIER;
import static io.github.miguelteles.beststickerapp.repository.contentProvider.StickerContentProvider.STICKER_PACK_ICON_IN_QUERY;
import static io.github.miguelteles.beststickerapp.repository.contentProvider.StickerContentProvider.STICKER_PACK_ICON_ORIGINAL_IMAGE_FILE;
import static io.github.miguelteles.beststickerapp.repository.contentProvider.StickerContentProvider.STICKER_PACK_IDENTIFIER;
import static io.github.miguelteles.beststickerapp.repository.contentProvider.StickerContentProvider.STICKER_PACK_IDENTIFIER_IN_QUERY;
import static io.github.miguelteles.beststickerapp.repository.contentProvider.StickerContentProvider.STICKER_PACK_NAME_IN_QUERY;
import static io.github.miguelteles.beststickerapp.repository.contentProvider.StickerContentProvider.STICKER_PACK_PUBLISHER_IN_QUERY;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import io.github.miguelteles.beststickerapp.BuildConfig;
import io.github.miguelteles.beststickerapp.domain.entity.Sticker;
import io.github.miguelteles.beststickerapp.domain.entity.StickerPack;

public class StickerContentProviderReader {

    /**
     * Get the list of sticker packs for the sticker content provider
     */
    @NonNull
    public static ArrayList<StickerPack> fetchStickerPacks(Context context) throws IllegalStateException {
        final Cursor cursor = context.getContentResolver().query(StickerContentProvider.AUTHORITY_URI, null, null, null, null);
        if (cursor == null) {
            throw new IllegalStateException("could not fetch from content provider, " + BuildConfig.CONTENT_PROVIDER_AUTHORITY);
        }
        final ArrayList<StickerPack> stickerPackList = fetchFromContentProvider(cursor);
        for (StickerPack stickerPack : stickerPackList) {
            stickerPack.setStickers(getStickersForPack(context, stickerPack));
        }
        return stickerPackList;
    }

    @NonNull
    private static List<Sticker> getStickersForPack(Context context, StickerPack stickerPack) {
        final List<Sticker> stickers = fetchFromContentProviderForStickers(stickerPack.getIdentifier(), context.getContentResolver());
        for (Sticker sticker : stickers) {
            final byte[] bytes;
            try {
                bytes = fetchStickerAsset(stickerPack.getIdentifier().toString(),
                        sticker.getStickerImageFile(),
                        context.getContentResolver());
                if (bytes.length <= 0) {
                    throw new IllegalStateException("Asset file is empty, pack: " + stickerPack.getName() + ", sticker: " + sticker.getStickerImageFile());
                }
                sticker.setSize(bytes.length);
            } catch (IOException | IllegalArgumentException e) {
                throw new IllegalStateException("Asset file doesn't exist. pack: " + stickerPack.getName() + ", sticker: " + sticker.getStickerImageFile(), e);
            }
        }
        return stickers;
    }


    @NonNull
    private static ArrayList<StickerPack> fetchFromContentProvider(Cursor cursor) {
        ArrayList<StickerPack> stickerPackList = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            final int identifier = cursor.getInt(cursor.getColumnIndexOrThrow(STICKER_PACK_IDENTIFIER_IN_QUERY));
            final String name = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_PACK_NAME_IN_QUERY));
            final String publisher = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_PACK_PUBLISHER_IN_QUERY));
            final String originalTrayImage = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_PACK_ICON_ORIGINAL_IMAGE_FILE));
            final String resizedTrayImage = cursor.getString(cursor.getColumnIndex(STICKER_PACK_ICON_IN_QUERY));
            final String folder = cursor.getString(cursor.getColumnIndexOrThrow(FOLDER));
            final String publisherEmail = cursor.getString(cursor.getColumnIndexOrThrow(PUBLISHER_EMAIL));
            final String publisherWebsite = cursor.getString(cursor.getColumnIndexOrThrow(PUBLISHER_WEBSITE));
            final String privacyPolicyWebsite = cursor.getString(cursor.getColumnIndexOrThrow(PRIVACY_POLICY_WEBSITE));
            final String licenseAgreementWebsite = cursor.getString(cursor.getColumnIndexOrThrow(LICENSE_AGREEMENT_WEBSITE));
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
    private static List<Sticker> fetchFromContentProviderForStickers(Integer stickerPackIdentifier, ContentResolver contentResolver) {
        Uri uri = StickerUriProvider.getStickerListUri(stickerPackIdentifier.toString());

        final String[] projection = {STICKER_FILE_NAME_IN_QUERY, STICKER_IDENTIFIER, STICKER_PACK_IDENTIFIER};
        final Cursor cursor = contentResolver.query(uri, projection, null, null, null);
        List<Sticker> stickers = new ArrayList<>();
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                final String imgFile = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_FILE_NAME_IN_QUERY));
                final Integer identifier = cursor.getInt(cursor.getColumnIndexOrThrow(STICKER_IDENTIFIER));
                final Integer packIdentifier = cursor.getInt(cursor.getColumnIndexOrThrow(STICKER_PACK_IDENTIFIER));
                stickers.add(new Sticker(identifier, packIdentifier, imgFile));
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
    private static byte[] fetchStickerAsset(@NonNull final String identifier,
                                    @NonNull final String stickerImageFileName,
                                    ContentResolver contentResolver) throws IOException {
        //o contentResolver.openInputStream vai pro método openAssetFile do contentProvider
        try (final InputStream inputStream = contentResolver.openInputStream(StickerUriProvider.getStickerAssetUri(identifier, stickerImageFileName));
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
}
