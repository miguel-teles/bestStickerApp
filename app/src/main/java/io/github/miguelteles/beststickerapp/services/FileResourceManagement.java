package io.github.miguelteles.beststickerapp.services;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import io.github.miguelteles.beststickerapp.exception.StickerFolderException;
import io.github.miguelteles.beststickerapp.exception.enums.StickerFolderExceptionEnum;
import io.github.miguelteles.beststickerapp.services.interfaces.ResourcesManagement;
import io.github.miguelteles.beststickerapp.utils.Utils;
import io.github.miguelteles.beststickerapp.validator.MethodInputValidator;

public class FileResourceManagement implements ResourcesManagement {

    private final Context context;
    private static FileResourceManagement filesManagement;

    private static ContentResolver contentResolver;

    public FileResourceManagement(Context context) {
        this.context = context;
        this.contentResolver = context.getContentResolver();
    }

    public static FileResourceManagement getInstance() {
        if (filesManagement == null) {
            filesManagement = new FileResourceManagement(Utils.getApplicationContext());
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
    public Uri getCertificateFolder() {
        return getOrCreateDirectory(getBaseFolder(), DirectoryNames.CERTIFICATES);
    }

    @Override
    public Uri getOrCreateFile(Uri folder, String fileName) throws StickerFolderException {
        MethodInputValidator.requireNotNull(folder, "Folder");
        MethodInputValidator.requireNotEmpty(fileName, "FileName");

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
        MethodInputValidator.requireNotEmpty(folderName, "FolderName");
        return getOrCreateDirectory(getOrCreateDirectory(getOrCreateLogsDirectory(),
                DirectoryNames.PACKS), folderName);
    }

    private Uri getOrCreateDirectory(Uri folder, String folderName) {
        File file = new File(folder.getPath(), folderName);
        file.mkdir();
        return Uri.fromFile(file);
    }

    @Override
    public Uri getOrCreateLogsDirectory() {
        return getOrCreateDirectory(getBaseFolder(),
                DirectoryNames.LOGS);
    }

    @Override
    public Uri getOrCreateLogErrorsDirectory() {
        return getOrCreateDirectory(getOrCreateLogsDirectory(),
                DirectoryNames.Logs.ERRORS);
    }

    @Override
    public List<Uri> getFilesFromDirectory(Uri folder) throws StickerFolderException {
        MethodInputValidator.requireNotNull(folder, "Folder");
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
                }
                if (!stickerPackFolderName.delete()) {
                    throw new StickerFolderException(null, StickerFolderExceptionEnum.DELETE_FOLDER, "Erro ao deletar file " + stickerPackFolderName.getName());
                }
            }
        } catch (Exception ex) {
            throw new StickerFolderException(ex, StickerFolderExceptionEnum.DELETE_FOLDER, "Pasta: " + stickerPackFolderName);
        }
    }

    @Override
    public void deleteFile(Uri uri) throws StickerFolderException {
        MethodInputValidator.requireNotNull(uri, "uri");
        this.deleteFile(new File(uri.getPath()));
    }

    @Override
    public String getFileExtension(Uri file, boolean withDot) {
        MethodInputValidator.requireNotNull(file, "file");
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
    public String getContentAsString(Uri file) throws StickerFolderException {
        MethodInputValidator.requireNotNull(file, "file");
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(contentResolver.openInputStream(file)))) {
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
    public byte[] getContentAsBytes(Uri file) throws StickerFolderException {
        MethodInputValidator.requireNotNull(file, "file");
        try (InputStream inputStream = contentResolver.openInputStream(file)) {
            return readBytesFromInputStream(inputStream);
        } catch (IOException e) {
            throw new StickerFolderException(e, StickerFolderExceptionEnum.GET_FILE, "Erro ao pegar conteúdo da uri");
        }
    }

    @Override
    public void writeToFile(Uri webpImage, InputStream inputStream) throws StickerFolderException {
        MethodInputValidator.requireNotNull(webpImage, "webpImage");
        MethodInputValidator.requireNotNull(inputStream, "inputStream");
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
        public final static String CERTIFICATES = "certificates";

        public static class Files {
            public final static String CLIENT_CERTIFICATE = "clientCertificate";
        }

        public static class Logs {
            public final static String ERRORS = "errors";
        }
    }
}
