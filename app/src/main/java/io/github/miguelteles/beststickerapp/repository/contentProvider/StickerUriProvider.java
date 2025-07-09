package io.github.miguelteles.beststickerapp.repository.contentProvider;

import static io.github.miguelteles.beststickerapp.repository.contentProvider.StickerContentProvider.STICKERS;

import android.content.ContentResolver;
import android.net.Uri;

import io.github.miguelteles.beststickerapp.BuildConfig;

public class StickerUriProvider {

    public static Uri getStickerListUri(String identifier) {
        return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(BuildConfig.CONTENT_PROVIDER_AUTHORITY).appendPath(STICKERS).appendPath(identifier).build();
    }

    public static Uri getStickerAssetUri(String identifier, String imageName) {
        return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(BuildConfig.CONTENT_PROVIDER_AUTHORITY).appendPath(StickerContentProvider.STICKERS_ASSET).appendPath(identifier).appendPath(imageName).build();
    }

    public static Uri getStickerResizedAssetUri(String identifier, String imageName) {
        return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(BuildConfig.CONTENT_PROVIDER_AUTHORITY).appendPath(StickerContentProvider.STICKERS_ASSET).appendPath(identifier).appendPath(imageName).build();
    }

    public static Uri getStickerOriginalAssetUri(String identifier, String imageName) {
        return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(BuildConfig.CONTENT_PROVIDER_AUTHORITY).appendPath(StickerContentProvider.STICKERS_ASSET_ORIGINAL).appendPath(identifier).appendPath(imageName).build();
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

    public static Uri getStickerDeleteUri() {
        return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(BuildConfig.CONTENT_PROVIDER_AUTHORITY).appendPath(StickerContentProvider.METHODS_DELETE).appendPath(StickerContentProvider.STICKERS).build();
    }

    public static Uri getStickerInsertUri() {
        return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(BuildConfig.CONTENT_PROVIDER_AUTHORITY).appendPath(StickerContentProvider.METHODS_ADD).appendPath(STICKERS).build();
    }

}
