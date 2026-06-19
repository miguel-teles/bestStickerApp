package io.github.miguelteles.beststickerapp.services.interfaces;

import android.net.Uri;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import io.github.miguelteles.beststickerapp.domain.pojo.VisualMediaType;
import io.github.miguelteles.beststickerapp.exception.StickerFolderException;
import io.github.miguelteles.beststickerapp.exception.enums.StickerFolderExceptionEnum;

public interface ResourcesManagement {

    Uri getBaseFolder();
    Uri getCacheFolder();
    Uri getCertificateFolder();
    Uri getOrCreateFile(@NotNull Uri folder, @NotNull String fileName) throws StickerFolderException;
    File getFileFromURI(@NotNull Uri file) throws StickerFolderException;
    Uri getOrCreateStickerPackDirectory(String folderName);
    Uri getOrCreateLogsDirectory();
    Uri getOrCreateLogErrorsDirectory();
    List<Uri> getFilesFromDirectory(Uri folder) throws StickerFolderException;
    Uri getStickerRelatedFile(String stickerPackFolderName, String fileName) throws StickerFolderException;
    Uri getStickerRelatedFile(Uri stickerPackFolder, String fileName) throws StickerFolderException;
    Uri getFile(String folder, String fileName) throws StickerFolderException;

//    void copyImageToStickerPackFolder(Uri sourceFile, Uri destinationFile) throws StickerFolderException;

    void deleteFile(Uri file) throws StickerFolderException;

    String getFileExtension(Uri file, boolean withDot);

    String getContentAsString(Uri file) throws StickerFolderException;

    byte[] getContentAsBytes(Uri file) throws StickerFolderException;

    void writeToFile(Uri destinationFile, InputStream inputStream) throws StickerFolderException;

    Uri saveFileToDevice(Uri stickerPackFolder, String filename, byte[] file) throws StickerFolderException;

    File downloadFile(URL url, File destinationFile) throws StickerFolderException;

    VisualMediaType getTypeOfVisualMedia(Uri uri) throws StickerFolderException;

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

    class Media {
        private final Uri originalImageFile;
        private final byte[] convertedMedia;
        private final URL linkToDownloadMedia;

        private Media(Builder builder) {
            this.originalImageFile = builder.originalImageFile;
            this.convertedMedia = builder.convertedMedia;
            this.linkToDownloadMedia = builder.linkToDownloadMedia;
        }

        public static Builder builder() {
            return new Builder();
        }

        public Uri getOriginalImageFile() {
            return originalImageFile;
        }

        public byte[] getConvertedMedia() {
            return convertedMedia;
        }

        public URL getLinkToDownloadMedia() {
            return linkToDownloadMedia;
        }

        public static class Builder {

            private Uri originalImageFile;
            private byte[] convertedMedia;
            private URL linkToDownloadMedia;

            public Builder originalImageFile(Uri originalImageFile) {
                this.originalImageFile = originalImageFile;
                return this;
            }

            public Builder convertedMedia(byte[] convertedMedia) {
                this.convertedMedia = convertedMedia;
                return this;
            }

            public Builder linkToDownloadMedia(URL linkToDownloadMedia) {
                this.linkToDownloadMedia = linkToDownloadMedia;
                return this;
            }

            public Media build() {
                return new Media(this);
            }
        }
    }
}
