package io.github.miguelteles.beststickerapp.services;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import io.github.miguelteles.beststickerapp.exception.StickerFolderException;
import io.github.miguelteles.beststickerapp.exception.enums.StickerFolderExceptionEnum;
import io.github.miguelteles.beststickerapp.utils.Utils;

public class FoldersManagementService {

    private static FoldersManagementService instance;

    public static final int TRAY_IMAGE_MAX_FILE_SIZE = 50; //50KB
    public static final int STICKER_IMAGE_MAX_FILE_SIZE = 100; //50KB
    public static final int TRAY_IMAGE_SIZE = 96; //96pxs
    public static final int STICKER_IMAGE_SIZE = 512; //512pxs
    public static final String TESTE_IMAGE = "test_image.jpg";
    public static final String STICKER_ERROR_IMAGE = "sticker_error_image.webp";

    private final Context context;

    private FoldersManagementService() {
        context = Utils.getApplicationContext();
        makeAllDirs();
    }

    public static FoldersManagementService getInstance() {
        if (instance == null) {
            instance = new FoldersManagementService();
        }

        return new FoldersManagementService();
    }

    public void makeAllDirs() {
        makeDirPacks();
        makeDirLogs();
    }

    private void makeDirPacks() {
        File externalDir = context.getFilesDir();
        File folderPacks = new File(externalDir, DirectoryNames.PACKS);
        if (!folderPacks.exists()) {
            folderPacks.mkdir();
        }
    }

    private void makeDirLogs() {
        File filesDir = context.getFilesDir();
        File folderLogs = new File(filesDir, DirectoryNames.LOGS);
        if (!folderLogs.exists()) {
            folderLogs.mkdir();
        }

        makeDirErrorsLogs();
    }

    private void makeDirErrorsLogs() {
        File path = getErrosFolder();
        if (!path.exists()) {
            path.mkdir();
        }
    }

    public File getErrosFolder() {
        File folderErrorsLogs = new File(DirectoryNames.LOGS, DirectoryNames.Logs.ERROS);
        return new File(context.getFilesDir(), folderErrorsLogs.getPath());
    }

    public File getPackFolderByFolderName(String folderName) throws StickerFolderException {
        File filesDir = context.getFilesDir();
        File packs = new File(filesDir, DirectoryNames.PACKS);
        if (packs.exists()) {

            File stickerPack = new File(packs, folderName);
            if (stickerPack.exists()) {
                return stickerPack;
            } else {
                throw new StickerFolderException(null, StickerFolderExceptionEnum.GET_PATH, "Pasta do pacote " + folderName + " não encontrada");
            }

        } else {
            throw new StickerFolderException(null, StickerFolderExceptionEnum.GET_PATH, "Pasta de pacotes não encontrada");
        }
    }

    public File getStickerPackFolderByFolderName(String stickerPackFolderName) throws StickerFolderException {
        try {
            File filesDir = context.getFilesDir();

            File folderPacks = new File(filesDir, DirectoryNames.PACKS);
            if (folderPacks.exists()) {
                File stickerPackFolder = new File(folderPacks, stickerPackFolderName);
                if (!stickerPackFolder.exists() && !stickerPackFolder.mkdir()) {
                    throw new StickerFolderException(null, StickerFolderExceptionEnum.CREATE_FOLDER_PACOTE, null);
                }
                return stickerPackFolder;
            } else {
                throw new StickerFolderException(null, StickerFolderExceptionEnum.GET_FOLDER, "Pasta dos pacotes não existe!");
            }
        } catch (Exception ex) {
            throw new StickerFolderException(ex, StickerFolderExceptionEnum.MKDIR_PACKS, "Erro ao criar pasta do pacote " + stickerPackFolderName);
        }
    }

    public String getAbsolutePathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        cursor.close();
        return path;
    }

    public void deleteFile(File stickerPackFolderName) throws StickerFolderException {
        try {
            if (stickerPackFolderName.exists()) {
                if (stickerPackFolderName.isDirectory()) {
                    String[] subfiles = stickerPackFolderName.list();
                    if (subfiles != null) {
                        for (String fileStr : subfiles) {
                            File file = new File(stickerPackFolderName, fileStr);
                            if (file.isDirectory()) {
                                deleteFile(file);
                            } else {
                                if (!file.delete()) {
                                    throw new StickerFolderException(null, StickerFolderExceptionEnum.DELETE_FOLDER, "Erro ao deletar file " + file.getName());
                                }
                            }
                        }
                    }
                }

                if (!stickerPackFolderName.delete()) {
                    throw new StickerFolderException(null, StickerFolderExceptionEnum.DELETE_FOLDER, "Erro ao deletar file " + stickerPackFolderName.getName());
                }
            }
        } catch (Exception ex) {
            throw new StickerFolderException(ex, StickerFolderExceptionEnum.DELETE_FOLDER, "Pasta: " + stickerPackFolderName);
        }
    }

    public void deleteStickerPackFolder(String folderName) throws StickerFolderException {
        File folder = getPackFolderByFolderName(folderName);
        deleteFile(folder);
    }

    public String getFileExtension(File file, boolean withDot) {
        String fileName = file.getName();
        String result = fileName.substring(fileName.lastIndexOf("."));
        if (!withDot) {
            result = result.replace(".", "");
        }
        return result;
    }

    public byte[] readBytesFromInputStream(InputStream inputStream) throws StickerFolderException {
        try (final ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            if (inputStream == null) {
                throw new IOException("cannot read stream because input stream is null");
            }
            int read;
            byte[] data = new byte[16384];

            while ((read = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, read);
            }
            return buffer.toByteArray();
        } catch (IOException e) {
            throw new StickerFolderException(e, StickerFolderExceptionEnum.GET_FILE, e.getMessage());
        }
    }

    public static class Image {

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

    public abstract static class DirectoryNames {
        public final static String ROOT = "appFigurinhas";
        public final static String LOGS = "logs";
        public final static String PACKS = "packs";

        public static class Logs {
            public final static String CRITICAL_ERRORS = "critical_erros";
            public final static String ERROS = "errors";
        }
    }

}
