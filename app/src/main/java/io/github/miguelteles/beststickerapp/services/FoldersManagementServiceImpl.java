package io.github.miguelteles.beststickerapp.services;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import io.github.miguelteles.beststickerapp.exception.StickerFolderException;
import io.github.miguelteles.beststickerapp.exception.enums.StickerFolderExceptionEnum;
import io.github.miguelteles.beststickerapp.services.interfaces.FoldersManagementService;
import io.github.miguelteles.beststickerapp.utils.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class FoldersManagementServiceImpl implements FoldersManagementService {

    private static FoldersManagementServiceImpl instance;

    public static final int TRAY_IMAGE_MAX_FILE_SIZE = 50; //50KB
    public static final int STICKER_IMAGE_MAX_FILE_SIZE = 100; //50KB
    public static final int TRAY_IMAGE_SIZE = 96; //96pxs
    public static final int STICKER_IMAGE_SIZE = 512; //512pxs
    public static final String TESTE_IMAGE = "test_image.jpg";
    public static final String STICKER_ERROR_IMAGE = "sticker_error_image.webp";

    private final Context context;

    private FoldersManagementServiceImpl() {
        context = Utils.getApplicationContext();
    }

    public static FoldersManagementServiceImpl getInstance() {
        if (instance == null) {
            instance = new FoldersManagementServiceImpl();
        }

        return new FoldersManagementServiceImpl();
    }

    public void makeAllDirs() throws StickerFolderException {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            makeDirPacks();
            makeDirLogs();
            makeDirErrorsLogs();
            makeDirCriticalErrorsLogs();
        } else {
            throw new StickerFolderException(null, StickerFolderExceptionEnum.MKDIR_ROOT, null);
        }
    }

    public void makeDirLogs() throws StickerFolderException {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

            File externalStorage = context.getExternalFilesDir(null);
            File folderLogs = new File(externalStorage, DirectoryNames.LOGS);
            if (!folderLogs.exists()) {
                folderLogs.mkdir();
            }
        } else {
            throw new StickerFolderException(null, StickerFolderExceptionEnum.MKDIR_LOG, null);
        }
    }

    public void makeDirErrorsLogs() throws StickerFolderException {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

            File externalStorage = context.getExternalFilesDir(null);
            File folderErrorsLogs = new File(DirectoryNames.LOGS, DirectoryNames.Logs.ERROS);
            File path = new File(externalStorage, folderErrorsLogs.getPath());
            if (!path.exists()) {
                path.mkdir();
            }
        } else {
            throw new StickerFolderException(null, StickerFolderExceptionEnum.MKDIR_LOG_ERRORS, null);
        }
    }

    public void makeDirCriticalErrorsLogs() throws StickerFolderException {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            makeDirLogs();

            File externalStorage = context.getExternalFilesDir(null);
            File folderCriticalErrorsLogs = new File(DirectoryNames.LOGS, DirectoryNames.Logs.CRITICAL_ERRORS);
            File path = new File(externalStorage, folderCriticalErrorsLogs.getPath());
            if (!path.exists()) {
                path.mkdir();
            }

        } else {
            throw new StickerFolderException(null, StickerFolderExceptionEnum.MKDIR_LOG_CRITICAL_ERRORS, null);
        }
    }

    public File getPackFolderByFolderName(String folderName) throws StickerFolderException {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

            File externalDir = context.getExternalFilesDir(null);
            File packs = new File(externalDir, DirectoryNames.PACKS);
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

        } else {
            throw new StickerFolderException(null, StickerFolderExceptionEnum.GET_PATH, "Erro ao buscar pasta de pacotes");
        }
    }

    public File getStickerPackFolderByFolderName(String stickerPackFolderName) throws StickerFolderException {
        try {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

                File externalDir = context.getExternalFilesDir(null);

                File folderPacks = new File(externalDir, DirectoryNames.PACKS);
                if (folderPacks.exists()) {
                    File stickerPackFolder = new File(folderPacks, stickerPackFolderName);
                    if (!stickerPackFolder.exists() && !stickerPackFolder.mkdir()) {
                        throw new StickerFolderException(null, StickerFolderExceptionEnum.CREATE_FOLDER_PACOTE, null);
                    }
                    return stickerPackFolder;
                } else {
                    throw new StickerFolderException(null, StickerFolderExceptionEnum.GET_FOLDER, "Pasta dos pacotes não existe!");
                }
            } else {
                throw new StickerFolderException(null, StickerFolderExceptionEnum.MKDIR_PACKS, null);
            }
        } catch (Exception ex) {
            throw new StickerFolderException(ex, StickerFolderExceptionEnum.MKDIR_PACKS, "Erro ao criar pasta do pacote " + stickerPackFolderName);
        }
    }

    public void makeDirPacks() throws StickerFolderException {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File externalDir = context.getExternalFilesDir(null);

            File folderPacks = new File(externalDir, DirectoryNames.PACKS);
            if (!folderPacks.exists()) {
                folderPacks.mkdir();
            }

        } else {
            throw new StickerFolderException(null, StickerFolderExceptionEnum.MKDIR_PACKS, null);
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
                    for (String fileStr : stickerPackFolderName.list()) {
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

    @Override
    public byte[] readBytesFromInputStream(InputStream inputStream, String imageFileName) throws StickerFolderException {
        try (final ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            if (inputStream == null) {
                throw new IOException("cannot read sticker asset name: " + imageFileName);
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


}
