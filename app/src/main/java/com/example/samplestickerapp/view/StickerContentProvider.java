/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.example.samplestickerapp.view;

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

import com.example.samplestickerapp.BuildConfig;
import com.example.samplestickerapp.repository.MyDatabase;
import com.example.samplestickerapp.exception.StickerException;
import com.example.samplestickerapp.exception.StickerExceptionHandler;
import com.example.samplestickerapp.exception.enums.StickerCriticalExceptionEnum;
import com.example.samplestickerapp.model.Sticker;
import com.example.samplestickerapp.model.StickerPack;
import com.example.samplestickerapp.repository.implementations.StickerPackRepository;
import com.example.samplestickerapp.repository.implementations.StickerRepository;
import com.example.samplestickerapp.utils.Folders;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class StickerContentProvider extends ContentProvider {

    /**
     * Do not change the strings listed below, as these are used by WhatsApp. And changing these will break the interface between sticker app and WhatsApp.
     */
    public static final String IDENTIFIER = "identifier";
    public static final String NAME = "name";
    public static final String PUBLISHER = "publisher";
    public static final String ORIGINAL_TRAY_IMAGE_FILE = "originalTrayImageFile";
    public static final String RESIZED_TRAY_IMAGE_FILE = "resizedTrayImageFile";
    public static final String FOLDER = "folder";
    public static final String PUBLISHER_EMAIL = "publisherEmail";
    public static final String PUBLISHER_WEBSITE = "publisherWebsite";
    public static final String PRIVACY_POLICY_WEBSITE = "privacePolicyWebsite";
    public static final String LICENSE_AGREENMENT_WEBSITE = "licenseAgreementWebsite";
    public static final String IMAGE_DATA_VERSION = "imageDataVersion";
    public static final String AVOID_CACHE = "avoidCache";
    public static final String ANIMATED_STICKER_PACK = "animatedStickerPack";

    public static final String STICKER_FILE_NAME_IN_QUERY = "imageFile";
    public static final String STICKER_FILE_EMOJI_IN_QUERY = "emoji";
    public static final String STICKER_IDENTIFIER = "identifier";
    public static final String STICKER_PACK_IDENTIFIER = "packIdentifier";


    public static final Uri AUTHORITY_URI = new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(BuildConfig.CONTENT_PROVIDER_AUTHORITY).appendPath(StickerContentProvider.METADATA).build();
    private static String ABSOLUTE_FOLDER = null;

    /**
     * Do not change the values in the UriMatcher because otherwise, WhatsApp will not be able to fetch the stickers from the ContentProvider.
     */
    private static UriMatcher MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
    private static final String METADATA = "metadata";
    static final String METHODS_ADD = "methods_add";
    public static final String METHODS_DELETE = "methods_delete";
    public static final String METHODS_UPDATE = "methods_update";

    private static final int METADATA_CODE = 1;
    private static final int METADATA_CODE_FOR_SINGLE_PACK = 2;
    static final String STICKERS = "stickers";
    static final String STICKERS_ASSET = "stickers_asset";
    static final String STICKERS_ASSET_ORIGINAL = "stickers_asset_original";

    static String AUTHORITY = null;
    private static final int STICKERS_CODE = 3;
    static final String PACK = "pack";
    private static final int STICKERS_ASSET_CODE = 4;
    private static final int STICKERS_ASSET_ORIGINAL_CODE = 10;

    private static final int STICKER_PACK_TRAY_ICON_CODE = 5;
    private static final int ADD_NEW_STICKER_PACK = 6;
    private static final int UPDATE_STICKER_PACK = 6;
    private static final int ADD_NEW_STICKER = 7;
    private static final int UPDATE_STICKER_PACK_LIST = 8;
    private static final int DELETE_STICKER_PACKPACK = 9;

    private List<StickerPack> stickerPackList;

    private StickerRepository stickerRepository;
    private StickerPackRepository stickerPackRepository;

    boolean isStickerPackListOutdated = true;

    /**
     * NÃO MUDAR ESSES MATCHERS E NEM AS URIS SE NÃO O WHATSAPP NÃO CONSEGUE BUSCAR DESTE CONTENTPROVIDER
     **/
    @Override
    public boolean onCreate() {

        try {
            stickerRepository = new StickerRepository(MyDatabase.getInstance(getContext()).getMyDB());
            stickerPackRepository = new StickerPackRepository(MyDatabase.getInstance(getContext()).getMyDB());
        } catch (StickerException ex) {
            StickerExceptionHandler.handleException(ex, getContext());
            return false;
        }

        AUTHORITY = BuildConfig.CONTENT_PROVIDER_AUTHORITY;
        if (!AUTHORITY.startsWith(Objects.requireNonNull(getContext()).getPackageName())) {
            throw new IllegalStateException("your authority (" + AUTHORITY + ") for the content provider should start with your package name: " + getContext().getPackageName());
        }
        carregaMatcher();
        return true;
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
        MATCHER.addURI(authority, STICKERS_ASSET + "/" + stickerPack.getIdentifier() + "/" + stickerPack.getResizedTrayImageFile(), STICKER_PACK_TRAY_ICON_CODE); //this returns the binary information of the sticker: `AssetFileDescriptor`, which points to the asset file for the sticker.
        MATCHER.addURI(authority, STICKERS_ASSET_ORIGINAL + "/" + stickerPack.getIdentifier() + "/" + stickerPack.getOriginalTrayImageFile(), STICKER_PACK_TRAY_ICON_CODE); //this returns the binary information of the sticker: `AssetFileDescriptor`, which points to the asset file for the sticker.
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
        if (stickerPackList == null || isStickerPackListOutdated) {
            try {
                stickerPackList = stickerPackRepository.findAll();
                isStickerPackListOutdated = false;
            } catch (StickerException ex) {
                StickerExceptionHandler.handleException(ex, getContext());
            }
        }
        return stickerPackList;
    }

    private Cursor getPackForAllStickerPacks(@NonNull Uri uri) {
        return getStickerPackInfo(uri, getStickerPackList());
    }

    private Cursor getCursorForSingleStickerPack(@NonNull Uri uri) {
        final String identifier = uri.getLastPathSegment();
        for (StickerPack stickerPack : getStickerPackList()) {
            if (identifier.equals(stickerPack.getIdentifier())) {
                return getStickerPackInfo(uri, Collections.singletonList(stickerPack));
            }
        }

        return getStickerPackInfo(uri, new ArrayList<>());
    }

    @NonNull
    private Cursor getStickerPackInfo(@NonNull Uri uri, @NonNull List<StickerPack> stickerPackList) {
        MatrixCursor cursor = new MatrixCursor(
                new String[]{
                        IDENTIFIER,
                        NAME,
                        PUBLISHER,
                        ORIGINAL_TRAY_IMAGE_FILE,
                        RESIZED_TRAY_IMAGE_FILE,
                        FOLDER,
                        IMAGE_DATA_VERSION,
                        AVOID_CACHE,
                        PUBLISHER_EMAIL,
                        PUBLISHER_WEBSITE,
                        PRIVACY_POLICY_WEBSITE,
                        LICENSE_AGREENMENT_WEBSITE,
                        ANIMATED_STICKER_PACK,
                });
        for (StickerPack stickerPack : stickerPackList) {
            MatrixCursor.RowBuilder builder = cursor.newRow();
            builder.add(stickerPack.getIdentifier());
            builder.add(stickerPack.getName());
            builder.add(stickerPack.getPublisher());
            builder.add(stickerPack.getOriginalTrayImageFile());
            builder.add(stickerPack.getResizedTrayImageFile());
            builder.add(stickerPack.getFolder());
            builder.add(stickerPack.getImageDataVersion());
            builder.add(stickerPack.isAvoidCache() ? 1 : 0);
            builder.add(stickerPack.getPublisherEmail());
            builder.add(stickerPack.getPublisherWebsite());
            builder.add(stickerPack.getPrivacyPolicyWebsite());
            builder.add(stickerPack.getLicenseAgreementWebsite());
            builder.add(stickerPack.isAnimatedStickerPack() ? 1 : 0);
        }
        cursor.setNotificationUri(Objects.requireNonNull(getContext()).getContentResolver(), uri);
        return cursor;
    }

    @NonNull
    private Cursor getStickersForAStickerPack(@NonNull Uri uri) {
        final String identifier = uri.getLastPathSegment();
        MatrixCursor cursor = new MatrixCursor(new String[]{STICKER_FILE_NAME_IN_QUERY,
                STICKER_IDENTIFIER,
                STICKER_PACK_IDENTIFIER});
        for (StickerPack stickerPack : getStickerPackList()) {
            if (identifier.equals(stickerPack.getIdentifier())) {
                for (Sticker sticker : stickerPack.getStickers()) {
                    cursor.addRow(new Object[]{sticker.getStickerImageFile(), sticker.getIdentifier(), sticker.getPackIdentifier()});
                }
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
        final Integer identifier = Integer.parseInt(pathSegments.get(pathSegments.size() - 2));
        if (identifier == null) {
            throw new IllegalArgumentException("identifier is null, uri: " + uri);
        }
        if (TextUtils.isEmpty(fileName)) {
            throw new IllegalArgumentException("file name is empty, uri: " + uri);
        }
        //making sure the file that is trying to be fetched is in the list of stickers.
        for (StickerPack stickerPack : getStickerPackList()) {
            if (identifier.equals(stickerPack.getIdentifier())) {
                if (fileName.equals(stickerPack.getResizedTrayImageFile()) || fileName.equals(stickerPack.getOriginalTrayImageFile())) {
                    return fetchFile(fileName, stickerPack.getFolder());
                } else {
                    for (Sticker sticker : stickerPack.getStickers()) {
                        if (fileName.equals(sticker.getStickerImageFile())) {
                            return fetchFile(fileName, stickerPack.getFolder());
                        }
                    }
                }
            }
        }
        return null;
    }

    private ParcelFileDescriptor fetchFile(@NonNull String fileName, @NonNull String folder) throws StickerException {
        try {
            return ParcelFileDescriptor.open(new File(Folders.getPackFolderByFolderName(folder, getContext()), fileName), ParcelFileDescriptor.MODE_READ_ONLY);
        } catch (IOException e) {
            throw new StickerException(e, StickerCriticalExceptionEnum.GET_FILE, "Erro ao abrir arquivo " + folder + "/" + fileName);
        }
    }


    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Not supported");
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

    public static String getAbsoluteFolder() {
        return ABSOLUTE_FOLDER;
    }

    public void setStickerPackListOutdated(boolean stickerPackListOutdated) {
        isStickerPackListOutdated = stickerPackListOutdated;
    }
}
