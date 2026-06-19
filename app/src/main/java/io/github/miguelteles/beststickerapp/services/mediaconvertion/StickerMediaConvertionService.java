package io.github.miguelteles.beststickerapp.services.mediaconvertion;

import android.content.ContentResolver;
import android.net.Uri;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;

import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.exception.StickerFolderException;
import io.github.miguelteles.beststickerapp.services.interfaces.ResourcesManagement;

public abstract class StickerMediaConvertionService {

    protected final ResourcesManagement resourcesManagement;
    protected final ContentResolver contentResolver;

    public StickerMediaConvertionService(ResourcesManagement resourcesManagement,
                                         ContentResolver contentResolver) {
        this.resourcesManagement = resourcesManagement;
        this.contentResolver = contentResolver;
    }

    public abstract ResourcesManagement.Media generateConvertedMedia(@NotNull Uri stickerPackFolder,
                                                                     @NotNull Uri sourceImage,
                                                                     @NotNull String destinationImageFileName,
                                                                     @NotNull Integer imageWidthAndHeight,
                                                                     boolean keepOriginalCopy) throws StickerException;

    protected final Uri generateMediaCopy(Uri sourceImage,
                                          Uri copyDestinationFolder,
                                          String copyDestinationFileName) throws Exception {
        Uri resizedImageOriginalFormat = resourcesManagement.getOrCreateFile(copyDestinationFolder, copyDestinationFileName);
        resourcesManagement.writeToFile(resizedImageOriginalFormat, contentResolver.openInputStream(sourceImage));
        return resizedImageOriginalFormat;
    }

    @NonNull
    protected final String buildResizedImageFileName(@NonNull Uri sourceImage, @NonNull String destinationImageFileName) {
        return destinationImageFileName + "Rzd" + this.resourcesManagement.getFileExtension(sourceImage, true);
    }

    @NonNull
    protected final String buildImageFileName(@NonNull Uri sourceImage, @NonNull String destinationImageFileName) {
        return destinationImageFileName + this.resourcesManagement.getFileExtension(sourceImage, true);
    }
}
