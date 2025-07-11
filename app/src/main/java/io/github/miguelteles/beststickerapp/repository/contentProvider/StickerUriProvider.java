package io.github.miguelteles.beststickerapp.repository.contentProvider;

import static io.github.miguelteles.beststickerapp.repository.contentProvider.StickerContentProvider.STICKERS;

import android.content.ContentResolver;
import android.net.Uri;

import io.github.miguelteles.beststickerapp.BuildConfig;

public class StickerUriProvider {

    private static StickerUriProvider instance;
    private StickerUriProvider(){}

    public static StickerUriProvider getInstance() {
        if (instance == null) {
            instance = new StickerUriProvider();
        }
        return instance;
    }

    public Uri getStickerListUri(String identifier) {
        return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(BuildConfig.CONTENT_PROVIDER_AUTHORITY).appendPath(STICKERS).appendPath(identifier).build();
    }

    public Uri getStickerAssetUri(Integer identifier, String imageName) {
        return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(BuildConfig.CONTENT_PROVIDER_AUTHORITY).appendPath(StickerContentProvider.STICKERS_ASSET).appendPath(identifier.toString()).appendPath(imageName).build();
    }

    public Uri getStickerPackResizedAssetUri(String identifier, String imageName) {
        return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(BuildConfig.CONTENT_PROVIDER_AUTHORITY).appendPath(StickerContentProvider.STICKERS_ASSET).appendPath(identifier).appendPath(imageName).build();
    }

    public Uri getStickerPackOriginalAssetUri(String identifier, String imageName) {
        return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(BuildConfig.CONTENT_PROVIDER_AUTHORITY).appendPath(StickerContentProvider.STICKERS_ASSET_ORIGINAL).appendPath(identifier).appendPath(imageName).build();
    }

    public Uri getStickerPackInsertUri() {
        return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(BuildConfig.CONTENT_PROVIDER_AUTHORITY).appendPath(StickerContentProvider.METHODS_ADD).appendPath(StickerContentProvider.PACK).build();
    }

    public Uri getStickerPackUpdateUri() {
        return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(BuildConfig.CONTENT_PROVIDER_AUTHORITY).appendPath(StickerContentProvider.METHODS_UPDATE).appendPath(StickerContentProvider.PACK).build();
    }

    public Uri getStickerPackDeleteUri() {
        return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(BuildConfig.CONTENT_PROVIDER_AUTHORITY).appendPath(StickerContentProvider.METHODS_DELETE).appendPath(StickerContentProvider.PACK).build();
    }

    public Uri getStickerDeleteUri() {
        return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(BuildConfig.CONTENT_PROVIDER_AUTHORITY).appendPath(StickerContentProvider.METHODS_DELETE).appendPath(StickerContentProvider.STICKERS).build();
    }

    public Uri getStickerInsertUri() {
        return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(BuildConfig.CONTENT_PROVIDER_AUTHORITY).appendPath(StickerContentProvider.METHODS_ADD).appendPath(STICKERS).build();
    }

}
