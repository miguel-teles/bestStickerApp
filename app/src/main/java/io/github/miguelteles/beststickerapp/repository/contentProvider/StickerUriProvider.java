package io.github.miguelteles.beststickerapp.repository.contentProvider;

import static io.github.miguelteles.beststickerapp.repository.contentProvider.StickerContentProvider.STICKERS;

import android.content.ContentResolver;
import android.net.Uri;

import java.util.UUID;

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

    public Uri getStickerListUri(UUID identifier) {
        return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(BuildConfig.CONTENT_PROVIDER_AUTHORITY).appendPath(STICKERS).appendPath(identifier.toString()).build();
    }

    public Uri getStickerAssetUri(UUID identifier, String imageName) {
        return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(BuildConfig.CONTENT_PROVIDER_AUTHORITY).appendPath(StickerContentProvider.STICKERS_ASSET).appendPath(identifier.toString()).appendPath(imageName).build();
    }

}
