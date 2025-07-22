package io.github.miguelteles.beststickerapp.services;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import androidx.annotation.NonNull;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import io.github.miguelteles.beststickerapp.exception.StickerFolderException;
import io.github.miguelteles.beststickerapp.exception.enums.StickerFolderExceptionEnum;
import io.github.miguelteles.beststickerapp.services.interfaces.ResourcesManagement;
import io.github.miguelteles.beststickerapp.utils.Utils;

public class FileResourceManagement implements ResourcesManagement {

    private final Context context;
    private static FileResourceManagement filesManagement;

    private static ContentResolver contentResolver;

    private FileResourceManagement(Context context) {
        this.context = context;
        this.contentResolver = context.getContentResolver();
    }

    public static FileResourceManagement getInstance(Context context) {
        if (filesManagement == null) {
            filesManagement = new FileResourceManagement(context);
        }
        return filesManagement;
    }

    @Override
    public Uri getBaseFolder() {
        return Uri.fromFile(context.getFilesDir());
    }

    @Override
    public Uri getCacheFolder() {
        return Uri.fromFile(context.getCacheDir());
    }

    @Override
    public Uri getOrCreateFile(Uri folder, String fileName) throws StickerFolderException {
        File file = new File(folder.getPath(), fileName);
        try {
            file.createNewFile();
        } catch (IOException ex) {
            throw new StickerFolderException(ex, StickerFolderExceptionEnum.MKFILE, null);
        }
        return Uri.fromFile(file);
    }

    @Override
    public Uri getOrCreateStickerPackDirectory(String folderName) {
        return getDirectoryCreateIfNotExist(getDirectoryCreateIfNotExist(getOrCreateLogsDirectory(),
                DirectoryNames.PACKS), folderName);
    }

    private Uri getDirectoryCreateIfNotExist(Uri folder, String folderName) {
        File file = new File(folder.getPath(), folderName);
        file.mkdir();
        return Uri.fromFile(file);
    }

    @Override
    public Uri getOrCreateLogsDirectory() {
        return getDirectoryCreateIfNotExist(getBaseFolder(),
                DirectoryNames.LOGS);
    }

    @Override
    public Uri getOrCreateLogErrorsDirectory() {
        return getDirectoryCreateIfNotExist(getOrCreateLogsDirectory(),
                DirectoryNames.Logs.ERRORS);
    }

    @Override
    public List<Uri> getFilesFromDirectory(Uri folder) throws StickerFolderException {
        File directory = new File(folder.getPath());
        if (directory.isDirectory()) {
            List<Uri> uris = new ArrayList<>();
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    uris.add(Uri.fromFile(file));
                }
            }
            return uris;
        } else {
            throw new StickerFolderException(null, StickerFolderExceptionEnum.GET_FOLDER, "Endereço aponta para um arquivo e não um diretório");
        }
    }

    private void deleteFile(File stickerPackFolderName) throws StickerFolderException {
        try {
            if (stickerPackFolderName.exists()) {
                if (stickerPackFolderName.isDirectory()) {
                    String[] subfiles = stickerPackFolderName.list();
                    if (subfiles != null) {
                        for (String fileStr : subfiles) {
                            deleteFile(new File(stickerPackFolderName, fileStr));
                        }
                    }
                } else if (!stickerPackFolderName.delete()) {
                    throw new StickerFolderException(null, StickerFolderExceptionEnum.DELETE_FOLDER, "Erro ao deletar file " + stickerPackFolderName.getName());
                }
            }
        } catch (Exception ex) {
            throw new StickerFolderException(ex, StickerFolderExceptionEnum.DELETE_FOLDER, "Pasta: " + stickerPackFolderName);
        }
    }

    @Override
    public void copyImageToStickerPackFolder(Uri sourceUri, Uri destinationUri) throws StickerFolderException {
        try (InputStream in = contentResolver.openInputStream(sourceUri);
             OutputStream out = contentResolver.openOutputStream(destinationUri)) {

            out.write(readBytesFromInputStream(in));
        } catch (Exception ex) {
            throw new StickerFolderException(ex, StickerFolderExceptionEnum.COPY, "Erro ao copiar file");
        }
    }

    @Override
    public void deleteFile(Uri uri) throws StickerFolderException {
        this.deleteFile(new File(uri.getPath()));
    }

    @Override
    public String getFileExtension(Uri file, boolean withDot) {
        String result = null;
        switch (file.getScheme()) {
            case ContentResolver.SCHEME_CONTENT:
                result = getFileExtensionFromFileSchemeContent(file, withDot);
                break;
            case ContentResolver.SCHEME_FILE:
                result = getFileExtensionFromFileSchemeFile(file, withDot);
                break;
        }
        return result;
    }

    private String getFileExtensionFromFileSchemeFile(Uri file, boolean withDot) {
        String fileName = file.getLastPathSegment();
        String result = fileName.substring(fileName.lastIndexOf("."));
        if (!withDot) {
            result = result.replace(".", "");
        }
        return result;
    }

    @NonNull
    private static String getFileExtensionFromFileSchemeContent(Uri file, boolean withDot) {
        String result;
        try (Cursor cursor = contentResolver.query(file, null, null, null, null)) {
            cursor.moveToFirst();
            String fileName = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));

            result = fileName.substring(fileName.lastIndexOf("."));
            if (!withDot) {
                result = result.replace(".", "");
            }
        }
        return result;
    }

    @Override
    public String getContentAsString(Uri exceptionLog) throws StickerFolderException {
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(Utils.getApplicationContext().getContentResolver().openInputStream(exceptionLog)))) {
            String line;
            if ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
        } catch (IOException e) {
            throw new StickerFolderException(e, StickerFolderExceptionEnum.GET_FILE, "Erro ao pegar conteúdo da uri");
        }
        return stringBuilder.toString();
    }

    @Override
    public void writeToFile(Uri webpImage, InputStream inputStream) throws StickerFolderException {
        File destinationFile = new File(webpImage.getPath());
        try (InputStream in = inputStream;
             FileOutputStream out = new FileOutputStream(destinationFile)) {

            byte[] buffer = new byte[4096];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        } catch (IOException e) {
            throw new StickerFolderException(e, StickerFolderExceptionEnum.CONVERT_FILE, "Erro ao converter imagem para .webp");
        }
    }

    public abstract static class DirectoryNames {
        public final static String LOGS = "logs";
        public final static String PACKS = "packs";

        public static class Logs {
            public final static String ERRORS = "errors";
        }
    }
}
