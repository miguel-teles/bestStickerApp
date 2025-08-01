package io.github.miguelteles.beststickerapp.repository.contentProvider;

import static io.github.miguelteles.beststickerapp.repository.contentProvider.StickerContentProvider.STICKERS;

import android.content.ContentResolver;
import android.net.Uri;

import java.util.UUID;

import io.github.miguelteles.beststickerapp.BuildConfig;

public class StickerUriProvider {

    public static Uri getStickerAsset(UUID identifier, String imageName) {
        return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(BuildConfig.CONTENT_PROVIDER_AUTHORITY).appendPath(StickerContentProvider.STICKERS_ASSET).appendPath(identifier.toString()).appendPath(imageName).build();
    }

    public static Uri getStickerOriginalAsset(UUID identifier, String imageName) {
        return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(BuildConfig.CONTENT_PROVIDER_AUTHORITY).appendPath(StickerContentProvider.STICKERS_ASSET_ORIGINAL).appendPath(identifier.toString()).appendPath(imageName).build();
    }

    public static Uri getMetadataCode() {
        return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(BuildConfig.CONTENT_PROVIDER_AUTHORITY).appendPath(StickerContentProvider.METADATA).build();
    }

    public static Uri getMetadataCodeFromPack(UUID identifier) {
        return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(BuildConfig.CONTENT_PROVIDER_AUTHORITY).appendPath(StickerContentProvider.METADATA).appendPath(identifier.toString()).build();
    }

    public static Uri getStickerListFromPack(UUID identifier) {
        return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(BuildConfig.CONTENT_PROVIDER_AUTHORITY).appendPath(STICKERS).appendPath(identifier.toString()).build();
    }



}
