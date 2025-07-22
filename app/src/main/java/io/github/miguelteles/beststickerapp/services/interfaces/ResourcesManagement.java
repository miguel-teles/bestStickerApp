package io.github.miguelteles.beststickerapp.services.interfaces;

import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import io.github.miguelteles.beststickerapp.exception.StickerFolderException;
import io.github.miguelteles.beststickerapp.exception.enums.StickerFolderExceptionEnum;

public interface ResourcesManagement {

    Uri getBaseFolder() throws StickerFolderException;
    Uri getCacheFolder();
    Uri getOrCreateFile(Uri folder, String fileName) throws StickerFolderException;
    Uri getOrCreateStickerPackDirectory(String folderName);
    Uri getOrCreateLogsDirectory();
    Uri getOrCreateLogErrorsDirectory();
    List<Uri> getFilesFromDirectory(Uri folder) throws StickerFolderException;

    void copyImageToStickerPackFolder(Uri sourceFile, Uri destinationFile) throws StickerFolderException;

    void deleteFile(Uri file) throws StickerFolderException;

    String getFileExtension(Uri file, boolean withDot);

    String getContentAsString(Uri exceptionLog) throws StickerFolderException;

    default byte[] readBytesFromInputStream(InputStream inputStream) throws StickerFolderException {
        try (final ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            if (inputStream == null) {
                throw new IOException("cannot read stream because input stream is null");
            }
            int read;
            byte[] data = new byte[4600];

            while ((read = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, read);
            }
            return buffer.toByteArray();
        } catch (IOException e) {
            throw new StickerFolderException(e, StickerFolderExceptionEnum.GET_FILE, e.getMessage());
        }
    }

    void writeToFile(Uri webpImage, InputStream inputStream) throws StickerFolderException;

    class Image {

        private final Uri originalImageFile;
        private final Uri resizedImageFile;

        private final byte[] residezImageFileInBytes;

        public Image(Uri originalImageFile, Uri resizedImageFile, byte[] residezImageFileInBytes) {
            this.originalImageFile = originalImageFile;
            this.resizedImageFile = resizedImageFile;
            this.residezImageFileInBytes = residezImageFileInBytes;
        }

        public Uri getOriginalImageFile() {
            return originalImageFile;
        }

        public Uri getResizedImageFile() {
            return resizedImageFile;
        }

        public byte[] getResidezImageFileInBytes() {
            return residezImageFileInBytes;
        }
    }
}
