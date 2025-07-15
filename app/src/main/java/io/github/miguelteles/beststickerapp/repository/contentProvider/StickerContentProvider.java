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
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.miguelteles.beststickerapp.BuildConfig;
import io.github.miguelteles.beststickerapp.exception.StickerExceptionHandler;
import io.github.miguelteles.beststickerapp.exception.StickerFolderException;
import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.exception.enums.StickerFolderExceptionEnum;
import io.github.miguelteles.beststickerapp.domain.entity.Sticker;
import io.github.miguelteles.beststickerapp.domain.entity.StickerPack;
import io.github.miguelteles.beststickerapp.services.FoldersManagementServiceImpl;
import io.github.miguelteles.beststickerapp.services.StickerPackService;
import io.github.miguelteles.beststickerapp.services.interfaces.FoldersManagementService;
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
    private static final String METADATA = "metadata";
    public static final String FOLDER = "folder";
    static final String METHODS_ADD = "methods_add";
    public static final String METHODS_DELETE = "methods_delete";
    public static final String METHODS_UPDATE = "methods_update";
    public static final String STICKERS = "stickers";
    static final String STICKERS_ASSET = "stickers_asset";
    static final String STICKERS_ASSET_ORIGINAL = "stickers_asset_original";
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
    private static final int ADD_NEW_STICKER_PACK = 6;
    private static final int UPDATE_STICKER_PACK = 6;
    private static final int ADD_NEW_STICKER = 7;
    private static final int UPDATE_STICKER_PACK_LIST = 8;
    private static final int DELETE_STICKER_PACKPACK = 9;

    private List<StickerPack> stickerPackList;
    private StickerPackService stickerPackService;
    private FoldersManagementService foldersManagementService;
    boolean isStickerPackListOutdated = true;

    /**
     * NÃO MUDAR ESSES MATCHERS E NEM AS URIS SE NÃO O WHATSAPP NÃO CONSEGUE BUSCAR DESTE CONTENTPROVIDER
     **/
    @Override
    public boolean onCreate() {
        try {
            Utils.setApplicationContext(getContext());
            stickerPackService = StickerPackService.getInstance();
            foldersManagementService = FoldersManagementServiceImpl.getInstance();

            AUTHORITY = BuildConfig.CONTENT_PROVIDER_AUTHORITY;
            if (!AUTHORITY.startsWith(Objects.requireNonNull(getContext()).getPackageName())) {
                throw new IllegalStateException("your authority (" + AUTHORITY + ") for the content provider should start with your package name: " + getContext().getPackageName());
            }
            carregaMatcher();
            return true;
        } catch (StickerException ex) {
            StickerExceptionHandler.handleException(ex, this.getContext());
            return false;
        }
    }

    private void carregaMatcher() {
        MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        //the call to get the metadata for the sticker packs.
        MATCHER.addURI(AUTHORITY, METADATA, METADATA_CODE); // this returns information about all the sticker packs in your app.

        //the call to get the metadata for single sticker pack. * represent the identifier
        MATCHER.addURI(AUTHORITY, METADATA + "/*", METADATA_CODE_FOR_SINGLE_PACK); //this returns information about a single pack.

        MATCHER.addURI(AUTHORITY, METHODS_ADD + "/" + PACK, ADD_NEW_STICKER_PACK);
        MATCHER.addURI(AUTHORITY, METHODS_ADD + "/" + STICKERS, ADD_NEW_STICKER);
        MATCHER.addURI(AUTHORITY, METHODS_UPDATE + "/" + PACK, UPDATE_STICKER_PACK);
        MATCHER.addURI(AUTHORITY, METHODS_DELETE + "/" + PACK, DELETE_STICKER_PACKPACK);

        //gets the list of stickers for a sticker pack, * respresent the identifier.
        MATCHER.addURI(AUTHORITY, STICKERS + "/*", STICKERS_CODE); //this returns information about the  stickers in a pack.
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
        switch (matchCode) {
            case METADATA_CODE:
                return "vnd.android.cursor.dir/vnd." + BuildConfig.CONTENT_PROVIDER_AUTHORITY + "." + METADATA;
            case METADATA_CODE_FOR_SINGLE_PACK:
                return "vnd.android.cursor.item/vnd." + BuildConfig.CONTENT_PROVIDER_AUTHORITY + "." + METADATA;
            case STICKERS_CODE:
                return "vnd.android.cursor.dir/vnd." + BuildConfig.CONTENT_PROVIDER_AUTHORITY + "." + STICKERS;
            case STICKERS_ASSET_CODE:
                return "image/webp";
            case STICKER_PACK_TRAY_ICON_CODE:
                return "image/png";
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    private List<StickerPack> getStickerPackList() {
        try {
            if (stickerPackList == null || isStickerPackListOutdated) {
                stickerPackList = stickerPackService.fetchAllStickerPacksWithoutAssets();
                isStickerPackListOutdated = false;
            }
            return stickerPackList;
        } catch (StickerException ex) {
            StickerExceptionHandler.handleException(ex, this.getContext());
            throw new RuntimeException(ex);
        }
    }

    private Cursor getPackForAllStickerPacks(@NonNull Uri uri) {
        return getStickerPackInfo(uri, getStickerPackList());
    }

    private Cursor getCursorForSingleStickerPack(@NonNull Uri uri) {
        final String identifier = uri.getLastPathSegment();
        for (StickerPack stickerPack : getStickerPackList()) {
            if (identifier.equals(stickerPack.getIdentifier().toString())) {
                return getStickerPackInfo(uri, Collections.singletonList(stickerPack));
            }
        }

        return getStickerPackInfo(uri, new ArrayList<>());
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
                        AVOID_CACHE,
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
            builder.add(stickerPack.getPublisherEmail()); //PUBLISHER_EMAIL
            builder.add(stickerPack.getPublisherWebsite()); //PUBLISHER_WEBSITE
            builder.add(stickerPack.getPrivacyPolicyWebsite()); //PRIVACY_POLICY_WEBSITE
            builder.add(stickerPack.getLicenseAgreementWebsite()); //LICENSE_AGREEMENT_WEBSITE
            builder.add(stickerPack.getImageDataVersion()); //IMAGE_DATA_VERSION
            builder.add(stickerPack.isAvoidCache() ? 1 : 0); //AVOID_CACHE
            builder.add(stickerPack.isAnimatedStickerPack() ? 1 : 0); //ANIMATED_STICKER_PACK
            builder.add(stickerPack.getOriginalTrayImageFile()); //STICKER_PACK_ICON_RESIZED_IMAGE_FILE
            builder.add(stickerPack.getFolderName()); //FOLDER
        }
        cursor.setNotificationUri(Objects.requireNonNull(getContext()).getContentResolver(), uri);
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
        cursor.setNotificationUri(Objects.requireNonNull(getContext()).getContentResolver(), uri);
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
            return ParcelFileDescriptor.open(new File(foldersManagementService.getPackFolderByFolderName(folder), fileName), ParcelFileDescriptor.MODE_READ_ONLY);
        } catch (IOException e) {
            return fetchFileStickerErrorImage();
        }
    }

    private ParcelFileDescriptor fetchFileStickerErrorImage() throws StickerFolderException {
        try {
            return ParcelFileDescriptor.open(copyStickerErroImageAssetToCache(), ParcelFileDescriptor.MODE_READ_ONLY);
        } catch (StickerException e) {
            throw e;
        } catch (IOException e) {
            throw new StickerFolderException(e, StickerFolderExceptionEnum.GET_FILE, "Erro ao abrir arquivo sticker_error_image");
        }
    }

    private File copyStickerErroImageAssetToCache() throws StickerFolderException {
        File cacheFile = new File(getContext().getCacheDir(), FoldersManagementServiceImpl.STICKER_ERROR_IMAGE);

        if (!cacheFile.exists()) {
            try (InputStream in = getContext().getAssets().open(FoldersManagementServiceImpl.STICKER_ERROR_IMAGE);
                 OutputStream out = new FileOutputStream(cacheFile)) {

                byte[] buffer = new byte[4096];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
            } catch (IOException e) {
                throw new StickerFolderException(e, StickerFolderExceptionEnum.GET_FILE, "Erro ao copiar sticker_error.");
            }
        }
        return cacheFile;
    }


    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, String[] selectionArgs) {
        this.isStickerPackListOutdated = true;
        return 1;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        int method = MATCHER.match(uri);
        switch (method) {
            case ADD_NEW_STICKER_PACK:
                insertStickerPackUri(AUTHORITY, StickerPack.fromContentValues(values));
                isStickerPackListOutdated = true;
                return uri;
            case ADD_NEW_STICKER:
                insertStickerUri(AUTHORITY, Sticker.fromContentValues(values));
                isStickerPackListOutdated = true;
                return uri;
            default:
                throw new UnsupportedOperationException("Método não esperado / não implementado");
        }
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int method = MATCHER.match(uri);
        switch (method) {
            case UPDATE_STICKER_PACK_LIST:
            case UPDATE_STICKER_PACK:
                StickerPack stickerPack = StickerPack.fromContentValues(values);
                insertStickerPackUri(AUTHORITY, stickerPack);
                isStickerPackListOutdated = true;
                carregaMatcher();
                return 1;
            default:
                throw new UnsupportedOperationException("Método não esperado / não implementado");
        }
    }
}
