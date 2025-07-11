package io.github.miguelteles.beststickerapp.services.interfaces;

import android.net.Uri;

import java.io.File;
import java.io.InputStream;

import io.github.miguelteles.beststickerapp.exception.StickerFolderException;

public interface FoldersManagementService {

    File getPackFolderByFolderName(String folderName) throws StickerFolderException;

    void makeAllDirs() throws StickerFolderException;

    File getStickerPackFolderByFolderName(String stickerPackFolderName) throws StickerFolderException;

    void makeDirPacks() throws StickerFolderException;

    void makeDirLogs() throws StickerFolderException;

    void makeDirErrorsLogs() throws StickerFolderException;

    void makeDirCriticalErrorsLogs() throws StickerFolderException;

    String getAbsolutePathFromURI(Uri contentUri);

    String getFileExtension(File file, boolean withDot);

    Image generateStickerImages(File stickerPackFolder,
                                Uri selectedImageSourceUri,
                                String destinationImageFileName,
                                Integer imageWidthAndHeight,
                                boolean keepOriginalCopy) throws StickerFolderException;

    Image generateStickerImages(File stickerPackFolder,
                                String selectedImageSourceAbsolutePath,
                                String destinationImageFileName,
                                Integer imageWidthAndHeight,
                                boolean keepOriginalCopy) throws StickerFolderException;

    void deleteFile(File stickerPackFolderName) throws StickerFolderException;

    void deleteStickerPackFolder(String folderName) throws StickerFolderException;

    byte[] readBytesFromInputStream(InputStream inputStream, String imageFileName) throws StickerFolderException;

    class Image {

        private final File originalImageFile;
        private final File resizedImageFile;

        private final byte[] residezImageFileInBytes;

        public Image(File originalImageFile, File resizedImageFile, byte[] residezImageFileInBytes) {
            this.originalImageFile = originalImageFile;
            this.resizedImageFile = resizedImageFile;
            this.residezImageFileInBytes = residezImageFileInBytes;
        }

        public File getOriginalImageFile() {
            return originalImageFile;
        }

        public File getResizedImageFile() {
            return resizedImageFile;
        }

        public byte[] getResidezImageFileInBytes() {
            return residezImageFileInBytes;
        }
    }

    abstract class DirectoryNames {
        public final static String ROOT = "appFigurinhas";
        public final static String LOGS = "logs";
        public final static String PACKS = "packs";

        public static class Logs {
            public final static String CRITICAL_ERRORS = "critical_erros";
            public final static String ERROS = "errors";
        }
    }
}
