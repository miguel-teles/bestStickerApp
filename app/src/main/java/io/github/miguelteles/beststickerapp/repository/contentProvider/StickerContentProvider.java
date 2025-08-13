/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.miguelteles.beststickerapp.repository.contentProvider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.miguelteles.beststickerapp.BuildConfig;
import io.github.miguelteles.beststickerapp.exception.handler.StickerExceptionHandler;
import io.github.miguelteles.beststickerapp.exception.StickerFolderException;
import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.exception.enums.StickerFolderExceptionEnum;
import io.github.miguelteles.beststickerapp.domain.entity.Sticker;
import io.github.miguelteles.beststickerapp.domain.entity.StickerPack;
import io.github.miguelteles.beststickerapp.services.FileResourceManagement;
import io.github.miguelteles.beststickerapp.services.StickerPackService;
import io.github.miguelteles.beststickerapp.services.interfaces.ResourcesManagement;
import io.github.miguelteles.beststickerapp.utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class StickerContentProvider extends ContentProvider {

    /* Constantes do contentProvider padrão. NÃO MUDAR! */
    public static final String STICKER_PACK_IDENTIFIER_IN_QUERY = "sticker_pack_identifier";
    public static final String STICKER_PACK_NAME_IN_QUERY = "sticker_pack_name";
    public static final String STICKER_PACK_PUBLISHER_IN_QUERY = "sticker_pack_publisher";
    public static final String STICKER_PACK_ICON_IN_QUERY = "sticker_pack_icon";
    public static final String ANDROID_APP_DOWNLOAD_LINK_IN_QUERY = "android_play_store_link";
    public static final String IOS_APP_DOWNLOAD_LINK_IN_QUERY = "ios_app_download_link";
    public static final String PUBLISHER_EMAIL = "sticker_pack_publisher_email";
    public static final String PUBLISHER_WEBSITE = "sticker_pack_publisher_website";
    public static final String PRIVACY_POLICY_WEBSITE = "sticker_pack_privacy_policy_website";
    public static final String LICENSE_AGREEMENT_WEBSITE = "sticker_pack_license_agreement_website";
    public static final String IMAGE_DATA_VERSION = "image_data_version";
    public static final String AVOID_CACHE = "whatsapp_will_not_cache_stickers";
    public static final String ANIMATED_STICKER_PACK = "animated_sticker_pack";
    public static final String STICKER_FILE_NAME_IN_QUERY = "sticker_file_name";
    public static final String STICKER_FILE_EMOJI_IN_QUERY = "sticker_emoji";
    public static final String STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY = "sticker_accessibility_text";

    /* Constantes customizadas */
    public static final String STICKER_IDENTIFIER = "identifier";
    public static final String STICKER_PACK_IDENTIFIER = "packIdentifier";
    public static final String STICKER_PACK_ICON_ORIGINAL_IMAGE_FILE = "originalTrayImageFile";
    public static final String METADATA = "metadata";
    public static final String FOLDER = "folder";
    public static final String STICKERS = "stickers";
    public static final String STICKERS_ASSET = "stickers_asset";
    public static final String STICKERS_ASSET_ORIGINAL = "stickers_asset_original";
    static final String PACK = "pack";

    public static final Uri AUTHORITY_URI = new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(BuildConfig.CONTENT_PROVIDER_AUTHORITY).appendPath(StickerContentProvider.METADATA).build();

    /**
     * Do not change the values in the UriMatcher because otherwise, WhatsApp will not be able to fetch the stickers from the ContentProvider.
     */
    private static UriMatcher MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
    static String AUTHORITY = null;

    private static final int METADATA_CODE = 1;
    private static final int METADATA_CODE_FOR_SINGLE_PACK = 2;
    private static final int STICKERS_CODE = 3;
    private static final int STICKERS_ASSET_CODE = 4;
    private static final int STICKER_PACK_TRAY_ICON_CODE = 5;

    private List<StickerPack> stickerPackList;
    private StickerPackService stickerPackService;
    private ResourcesManagement resourcesManagement;
    private Context context;
    private ContentResolver contentResolver;
    boolean isStickerPackListOutdated = true;

    public StickerContentProvider() {
    }

    public StickerContentProvider(StickerPackService stickerPackService,
                                  ResourcesManagement resourcesManagement,
                                  Context context) {
        this.stickerPackService = stickerPackService;
        this.resourcesManagement = resourcesManagement;
        this.context = context;
        this.contentResolver = context.getContentResolver();
        carregaMatcher();
    }

    /**
     * NÃO MUDAR ESSES MATCHERS E NEM AS URIS SE NÃO O WHATSAPP NÃO CONSEGUE BUSCAR DESTE CONTENTPROVIDER
     **/
    @Override
    public boolean onCreate() {
        try {
            context = getContext();
            contentResolver = context.getContentResolver();

            Utils.setApplicationContext(context);
            stickerPackService = StickerPackService.getInstance();
            resourcesManagement = FileResourceManagement.getInstance();

            carregaMatcher();
            return true;
        } catch (StickerException ex) {
            StickerExceptionHandler.handleException(ex, this.context);
            return false;
        }
    }

    private void carregaMatcher() {
        AUTHORITY = BuildConfig.CONTENT_PROVIDER_AUTHORITY;

        MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        //the call to get the metadata for the sticker packs.
        MATCHER.addURI(AUTHORITY, METADATA, METADATA_CODE); // this returns information about all the sticker packs in your app.

        //the call to get the metadata for single sticker pack. * represent the identifier
        MATCHER.addURI(AUTHORITY, METADATA + "/*", METADATA_CODE_FOR_SINGLE_PACK); //this returns information about a single pack.

        //gets the list of stickers for a sticker pack, * respresent the identifier.
        MATCHER.addURI(AUTHORITY, STICKERS + "/*", STICKERS_CODE); //this returns information about the  stickers in a pack.
    }

    private void loadStickerPackAndStickerMatchers() {
        for (StickerPack stickerPack : getStickerPackList()) {
            insertStickerPackUri(AUTHORITY, stickerPack);
            for (Sticker sticker : stickerPack.getStickers()) {
                insertStickerUri(AUTHORITY, sticker);
            }
        }
    }

    private void insertStickerPackUri(String authority, StickerPack stickerPack) {
        MATCHER.addURI(authority, STICKERS_ASSET + "/" + stickerPack.getIdentifier().toString() + "/" + stickerPack.getResizedTrayImageFile(), STICKER_PACK_TRAY_ICON_CODE); //this returns the binary information of the sticker: `AssetFileDescriptor`, which points to the asset file for the sticker.
        MATCHER.addURI(authority, STICKERS_ASSET_ORIGINAL + "/" + stickerPack.getIdentifier().toString() + "/" + stickerPack.getOriginalTrayImageFile(), STICKER_PACK_TRAY_ICON_CODE); //this returns the binary information of the sticker: `AssetFileDescriptor`, which points to the asset file for the sticker.
    }

    private void insertStickerUri(String authority, Sticker sticker) {
        MATCHER.addURI(authority, STICKERS_ASSET + "/" + sticker.getPackIdentifier() + "/" + sticker.getStickerImageFile(), STICKERS_ASSET_CODE);
    }

    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        final int code = MATCHER.match(uri);
        loadStickerPackAndStickerMatchers();
        if (code == METADATA_CODE) {
            return getPackForAllStickerPacks(uri);
        } else if (code == METADATA_CODE_FOR_SINGLE_PACK) {
            return getCursorForSingleStickerPack(uri);
        } else if (code == STICKERS_CODE) {
            return getStickersForAStickerPack(uri);
        } else {
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }


    @Override
    public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode) {
        final int matchCode = MATCHER.match(uri);
        if (matchCode == STICKERS_ASSET_CODE || matchCode == STICKER_PACK_TRAY_ICON_CODE) {
            try {
                return getImageAsset(uri);
            } catch (StickerException ex) {
                throw new RuntimeException(ex);
            } catch (Exception ex) {
                throw new RuntimeException(ex.getMessage());
            }
        }
        return null;
    }


    @Override
    public String getType(@NonNull Uri uri) {
        final int matchCode = MATCHER.match(uri);
        return switch (matchCode) {
            case METADATA_CODE ->
                    "vnd.android.cursor.dir/vnd." + BuildConfig.CONTENT_PROVIDER_AUTHORITY + "." + METADATA;
            case METADATA_CODE_FOR_SINGLE_PACK ->
                    "vnd.android.cursor.item/vnd." + BuildConfig.CONTENT_PROVIDER_AUTHORITY + "." + METADATA;
            case STICKERS_CODE ->
                    "vnd.android.cursor.dir/vnd." + BuildConfig.CONTENT_PROVIDER_AUTHORITY + "." + STICKERS;
            case STICKERS_ASSET_CODE -> "image/webp";
            case STICKER_PACK_TRAY_ICON_CODE -> "image/png";
            default -> throw new IllegalArgumentException("Unknown URI: " + uri);
        };
    }

    private List<StickerPack> getStickerPackList() {
        try {
            stickerPackList = stickerPackService.fetchAllStickerPacksWithoutAssets();
            return stickerPackList;
        } catch (StickerException ex) {
            StickerExceptionHandler.handleException(ex, this.context);
            throw new RuntimeException(ex);
        }
    }

    private Cursor getPackForAllStickerPacks(@NonNull Uri uri) {
        return getStickerPackInfo(uri, getStickerPackList());
    }

    private Cursor getCursorForSingleStickerPack(@NonNull Uri uri) {
        try {
            final String identifier = uri.getLastPathSegment();
            StickerPack stickerPack = stickerPackService.fetchStickerPackWithoutAssets(UUID.fromString(identifier));
            if (identifier.equals(stickerPack.getIdentifier().toString())) {
                return getStickerPackInfo(uri, Collections.singletonList(stickerPack));
            }

            return getStickerPackInfo(uri, new ArrayList<>());
        } catch (StickerException ex) {
            StickerExceptionHandler.handleException(ex, this.context);
            throw new RuntimeException(ex);
        }
    }

    @NonNull
    private Cursor getStickerPackInfo(@NonNull Uri uri, @NonNull List<StickerPack> stickerPackList) {
        MatrixCursor cursor = new MatrixCursor(
                new String[]{
                        STICKER_PACK_IDENTIFIER_IN_QUERY,
                        STICKER_PACK_NAME_IN_QUERY,
                        STICKER_PACK_PUBLISHER_IN_QUERY,
                        STICKER_PACK_ICON_IN_QUERY,
                        ANDROID_APP_DOWNLOAD_LINK_IN_QUERY,
                        IOS_APP_DOWNLOAD_LINK_IN_QUERY,
                        PUBLISHER_EMAIL,
                        PUBLISHER_WEBSITE,
                        PRIVACY_POLICY_WEBSITE,
                        LICENSE_AGREEMENT_WEBSITE,
                        IMAGE_DATA_VERSION,
                        ANIMATED_STICKER_PACK,
                        STICKER_PACK_ICON_ORIGINAL_IMAGE_FILE,
                        FOLDER,
                });
        for (StickerPack stickerPack : stickerPackList) {
            MatrixCursor.RowBuilder builder = cursor.newRow();
            builder.add(stickerPack.getIdentifier()); //STICKER_PACK_IDENTIFIER_IN_QUERY
            builder.add(stickerPack.getName()); //STICKER_PACK_NAME_IN_QUERY
            builder.add(stickerPack.getPublisher()); //STICKER_PACK_PUBLISHER_IN_QUERY
            builder.add(stickerPack.getResizedTrayImageFile()); //STICKER_PACK_ICON_IN_QUERY
            builder.add(""); //ANDROID_APP_DOWNLOAD_LINK_IN_QUERY
            builder.add(""); //IOS_APP_DOWNLOAD_LINK_IN_QUERY
            builder.add(""); //PUBLISHER_EMAIL
            builder.add(""); //PUBLISHER_WEBSITE
            builder.add(""); //PRIVACY_POLICY_WEBSITE
            builder.add(""); //LICENSE_AGREEMENT_WEBSITE
            builder.add(stickerPack.getImageDataVersion()); //IMAGE_DATA_VERSION
            builder.add(stickerPack.isAnimatedStickerPack() ? 1 : 0); //ANIMATED_STICKER_PACK
            builder.add(stickerPack.getOriginalTrayImageFile()); //STICKER_PACK_ICON_RESIZED_IMAGE_FILE
            builder.add(stickerPack.getFolderName()); //FOLDER
        }
        cursor.setNotificationUri(contentResolver, uri);
        return cursor;
    }

    @NonNull
    private Cursor getStickersForAStickerPack(@NonNull Uri uri) {
        final String identifier = uri.getLastPathSegment();
        MatrixCursor cursor = new MatrixCursor(new String[]{STICKER_FILE_NAME_IN_QUERY,
                STICKER_FILE_EMOJI_IN_QUERY,
                STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY,
                STICKER_IDENTIFIER,
                STICKER_PACK_IDENTIFIER});
        for (StickerPack stickerPack : getStickerPackList()) {
            if (identifier.equals(stickerPack.getIdentifier().toString())) {
                for (Sticker sticker : stickerPack.getStickers()) {
                    cursor.addRow(new Object[]{sticker.getStickerImageFile(),
                            "\uD83D\uDE00",
                            "hey im the text",
                            sticker.getIdentifier(),
                            sticker.getPackIdentifier()});
                }
                break;
            }
        }
        cursor.setNotificationUri(contentResolver, uri);
        return cursor;
    }

    private ParcelFileDescriptor getImageAsset(Uri uri) throws IllegalArgumentException, StickerException {
        final List<String> pathSegments = uri.getPathSegments();
        if (pathSegments.size() != 3) {
            throw new IllegalArgumentException("path segments should be 3, uri is: " + uri);
        }
        String fileName = pathSegments.get(pathSegments.size() - 1);
        final String identifier = pathSegments.get(pathSegments.size() - 2);
        if (identifier == null) {
            throw new IllegalArgumentException("identifier is null, uri: " + uri);
        }
        if (TextUtils.isEmpty(fileName)) {
            throw new IllegalArgumentException("file name is empty, uri: " + uri);
        }
        //making sure the file that is trying to be fetched is in the list of stickers.
        for (StickerPack stickerPack : getStickerPackList()) {
            if (identifier.equals(stickerPack.getIdentifier().toString())) {
                if (fileName.equals(stickerPack.getResizedTrayImageFile()) || fileName.equals(stickerPack.getOriginalTrayImageFile())) {
                    return fetchFile(fileName, stickerPack.getFolderName());
                } else {
                    for (Sticker sticker : stickerPack.getStickers()) {
                        if (fileName.equals(sticker.getStickerImageFile())) {
                            return fetchFile(fileName, stickerPack.getFolderName());
                        }
                    }
                }
            }
        }
        return null;
    }

    private ParcelFileDescriptor fetchFile(@NonNull String fileName, @NonNull String folder) throws StickerException {
        try {
            return contentResolver
                    .openFileDescriptor(Uri.withAppendedPath(resourcesManagement.getOrCreateStickerPackDirectory(folder), fileName), "r");
        } catch (IOException e) {
            return fetchFileStickerErrorImage();
        }
    }

    private ParcelFileDescriptor fetchFileStickerErrorImage() throws StickerFolderException {
        try {
            return contentResolver
                    .openFileDescriptor(copyStickerErroImageAssetToCache(), "r");
        } catch (StickerException e) {
            throw e;
        } catch (IOException e) {
            throw new StickerFolderException(e, StickerFolderExceptionEnum.GET_FILE, "Erro ao abrir arquivo sticker_error_image");
        }
    }

    private Uri copyStickerErroImageAssetToCache() throws StickerFolderException {
        Uri cacheFile = resourcesManagement.getOrCreateFile(resourcesManagement.getCacheFolder(), Sticker.STICKER_ERROR_IMAGE);

        try (InputStream in = context.getAssets().open(Sticker.STICKER_ERROR_IMAGE)) {
            resourcesManagement.writeToFile(cacheFile, in);
        } catch (IOException e) {
            throw new StickerFolderException(e, StickerFolderExceptionEnum.GET_FILE, "Erro ao copiar sticker_error.");
        }

        return cacheFile;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
