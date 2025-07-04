package com.example.samplestickerapp.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import com.example.samplestickerapp.exception.StickerException;
import com.example.samplestickerapp.exception.enums.StickerCriticalExceptionEnum;
import com.example.samplestickerapp.exception.enums.StickerExceptionEnum;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class Folders {

    public static final int TRAY_IMAGE_MAX_FILE_SIZE = 50; //50KB
    public static final int STICKER_IMAGE_MAX_FILE_SIZE = 100; //50KB
    public static final int TRAY_IMAGE_SIZE = 96; //96pxs
    public static final int STICKER_IMAGE_SIZE = 512; //512pxs

    private Folders() {
    }

    public static File getLogsFolderPath(Context context) throws StickerException {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

            File externalDir = context.getExternalFilesDir(null);
            File logs = new File(externalDir, DirectoryNames.LOGS);
            if (logs.exists()) {
                return logs;
            } else {
                throw new StickerException(null, StickerCriticalExceptionEnum.GET_PATH, "Pasta de logs não encontrada");
            }

        } else {
            throw new StickerException(null, StickerCriticalExceptionEnum.GET_PATH, "Erro ao buscar pasta de logs");
        }
    }

    public static File getPacksFolder(Context context) throws StickerException {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

            File externalDir = context.getExternalFilesDir(null);
            File packs = new File(externalDir, DirectoryNames.PACKS);
            if (packs.exists()) {
                return packs;
            } else {
                throw new StickerException(null, StickerCriticalExceptionEnum.GET_PATH, "Pasta de pacotes não encontrada");
            }

        } else {
            throw new StickerException(null, StickerCriticalExceptionEnum.GET_PATH, "Erro ao buscar pasta de pacotes");
        }
    }

    public static File getPackFolderByFolderName(String folderName, Context context) throws StickerException {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

            File externalDir = context.getExternalFilesDir(null);
            File packs = new File(externalDir, DirectoryNames.PACKS);
            if (packs.exists()) {

                File stickerPack = new File(packs, folderName);
                if (stickerPack.exists()) {
                    return stickerPack;
                } else {
                    throw new StickerException(null, StickerCriticalExceptionEnum.GET_PATH, "Pasta do pacote " + folderName + " não encontrada");
                }

            } else {
                throw new StickerException(null, StickerCriticalExceptionEnum.GET_PATH, "Pasta de pacotes não encontrada");
            }

        } else {
            throw new StickerException(null, StickerCriticalExceptionEnum.GET_PATH, "Erro ao buscar pasta de pacotes");
        }
    }

    public static void makeAllDirs(Context context) throws StickerException {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            makeDirPacks(context);
            makeDirLogs(context);
            makeDirErrorsLogs(context);
            makeDirCriticalErrorsLogs(context);
        } else {
            throw new StickerException(null, StickerCriticalExceptionEnum.MKDIR_ROOT, null);
        }
    }

    public static File getStickerPackFolderByFolderName(String stickerPackFolderName, Context context) throws StickerException {
        try {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

                File externalDir = context.getExternalFilesDir(null);

                File folderPacks = new File(externalDir, DirectoryNames.PACKS);
                if (folderPacks.exists()) {
                    File stickerPackFolder = new File(folderPacks, stickerPackFolderName);
                    if (!stickerPackFolder.exists() && !stickerPackFolder.mkdir()) {
                        throw new StickerException(null, StickerCriticalExceptionEnum.CREATE_FOLDER_PACOTE, null);
                    }
                    return stickerPackFolder;
                } else {
                    throw new StickerException(null, StickerCriticalExceptionEnum.GET_FOLDER, "Pasta dos pacotes não existe!");
                }
            } else {
                throw new StickerException(null, StickerCriticalExceptionEnum.MKDIR_PACKS, null);
            }
        } catch (Exception ex) {
            throw new StickerException(ex, StickerCriticalExceptionEnum.MKDIR_PACKS, "Erro ao criar pasta do pacote " + stickerPackFolderName);
        }
    }

    public static void makeDirPacks(Context context) throws StickerException {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File externalDir = context.getExternalFilesDir(null);

            File folderPacks = new File(externalDir, DirectoryNames.PACKS);
            if (!folderPacks.exists()) {
                folderPacks.mkdir();
            }

        } else {
            throw new StickerException(null, StickerCriticalExceptionEnum.MKDIR_PACKS, null);
        }
    }

    public static void makeDirLogs(Context context) throws StickerException {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

            File externalStorage = context.getExternalFilesDir(null);
            File folderLogs = new File(externalStorage, DirectoryNames.LOGS);
            if (!folderLogs.exists()) {
                folderLogs.mkdir();
            }
        } else {
            throw new StickerException(null, StickerCriticalExceptionEnum.MKDIR_LOG, null);
        }
    }

    public static void makeDirErrorsLogs(Context context) throws StickerException {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

            File externalStorage = context.getExternalFilesDir(null);
            File folderErrorsLogs = new File(DirectoryNames.LOGS, DirectoryNames.Logs.ERROS);
            File path = new File(externalStorage, folderErrorsLogs.getPath());
            if (!path.exists()) {
                path.mkdir();
            }
        } else {
            throw new StickerException(null, StickerCriticalExceptionEnum.MKDIR_LOG_ERRORS, null);
        }
    }

    public static void makeDirCriticalErrorsLogs(Context context) throws StickerException {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            makeDirLogs(context);

            File externalStorage = context.getExternalFilesDir(null);
            File folderCriticalErrorsLogs = new File(DirectoryNames.LOGS, DirectoryNames.Logs.CRITICAL_ERRORS);
            File path = new File(externalStorage, folderCriticalErrorsLogs.getPath());
            if (!path.exists()) {
                path.mkdir();
            }

        } else {
            throw new StickerException(null, StickerCriticalExceptionEnum.MKDIR_LOG_CRITICAL_ERRORS, null);
        }
    }

    public static String getRealPathFromURI(Uri contentUri, Context context) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        cursor.close();
        return path;
    }

    public static String getFileExtension(File file, boolean withDot) {
        String fileName = file.getName();
        String result = fileName.substring(fileName.lastIndexOf("."));
        if (!withDot) {
            result = result.replace(".", "");
        }
        return result;
    }

    public static Image generateStickerImages(File stickerPackFolder,
                                              String sourceImagePath,
                                              String destinationImageFileName,
                                              Integer imageWidthAndHeight,
                                              boolean keepOriginalCopy) throws StickerException {
        try {
            File sourceImage = new File(sourceImagePath);
            int rotation = getImageOrientation(sourceImagePath);

            String stickerPackImageFileName = destinationImageFileName + getFileExtension(sourceImage, true);
            String stickerPackImageResizedFileName = destinationImageFileName + "Rzd" + getFileExtension(sourceImage, true);

            if (keepOriginalCopy) {
                File stickerPackOriginalImageAbsoluteFile = new File(stickerPackFolder, stickerPackImageFileName);
                copyImageFromSourceToDestination(sourceImage, stickerPackOriginalImageAbsoluteFile);
                resizeAndRotateImage(stickerPackOriginalImageAbsoluteFile, determineImageSmallerSide(stickerPackOriginalImageAbsoluteFile), Folders.TRAY_IMAGE_MAX_FILE_SIZE, rotation);
            }
            File stickerPackResizedImageAbsoluteFile = new File(stickerPackFolder, stickerPackImageResizedFileName);
            copyImageFromSourceToDestination(sourceImage, stickerPackResizedImageAbsoluteFile);
            resizeAndRotateImage(stickerPackResizedImageAbsoluteFile, imageWidthAndHeight, Folders.TRAY_IMAGE_MAX_FILE_SIZE, rotation);

            return new Image(stickerPackImageFileName, stickerPackImageResizedFileName);
        } catch (StickerException ste) {
            throw ste;
        } catch (Exception ex) {
            throw new StickerException(ex, StickerExceptionEnum.CSP, "Erro ao copiar foto do pacote para a pasta do pacote " + stickerPackFolder.getName());
        }
    }

    private static Bitmap applyRotationToBitmap(Bitmap bitmap, int rotate) {
        Matrix matrix = new Matrix();
        matrix.postRotate(rotate);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return rotatedBitmap;
    }

    /**
     * Por algum motivo, as imagens tiradas das câmeras da Samsung são viradas em 90 graus. Então quando copiamos a imagem
     * **/
    public static int getImageOrientation(String imagePath) {
        int rotate = 0;
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rotate;
    }


    private static int determineImageSmallerSide(File stickerPackImageFile) {
        Bitmap bitmap = BitmapFactory.decodeFile(stickerPackImageFile.getAbsolutePath());
        int greaterSide = bitmap.getWidth();
        if (greaterSide < bitmap.getHeight()) {
            greaterSide = bitmap.getHeight();
        }
        return greaterSide;
    }

    private static void copyImageFromSourceToDestination(File sourceFile, File destinationFile) throws StickerException {
        try {
            Files.copy(sourceFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception ex) {
            throw new StickerException(ex, StickerCriticalExceptionEnum.COPY, "Erro ao copiar file");
        }
    }

    //TODO: precisa mudar o tamanho da imagem pra poder enviar para o whats com base na tamanho do parâmetro fileSize
    private static void resizeAndRotateImage(File imageToResize,
                                             int imageWidthAndHeight,
                                             int fileSize,
                                             int rotation) throws StickerException {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(imageToResize.getAbsolutePath());

            bitmap = applyRotationToBitmap(bitmap, rotation);
            bitmap = Bitmap.createScaledBitmap(bitmap, imageWidthAndHeight, imageWidthAndHeight, false);

            FileOutputStream out = new FileOutputStream(imageToResize);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception ex) {
            throw new StickerException(ex, StickerCriticalExceptionEnum.RESIZE, "Imagem: " + imageToResize.getName());
        }
    }

    public static void deleteFile(File stickerPackFolderName) throws StickerException {
        try {

            if (stickerPackFolderName.isDirectory()) {
                for (String fileStr : stickerPackFolderName.list()) {
                    File file = new File(stickerPackFolderName, fileStr);
                    if (file.isDirectory()) {
                        deleteFile(file);
                    } else {
                        if (!file.delete()) {
                            throw new StickerException(null, StickerCriticalExceptionEnum.DELETE_FOLDER, "Erro ao deletar file " + file.getName());
                        }
                    }
                }
            }

            if (!stickerPackFolderName.delete()) {
                throw new StickerException(null, StickerCriticalExceptionEnum.DELETE_FOLDER, "Erro ao deletar file " + stickerPackFolderName.getName());
            }
        } catch (Exception ex) {
            throw new StickerException(ex, StickerCriticalExceptionEnum.DELETE_FOLDER, "Pasta: " + stickerPackFolderName);
        }
    }

    public static void deleteStickerPackFolder(String folderName, Context applicationContext) throws StickerException {
        File folder = getPackFolderByFolderName(folderName, applicationContext);
        deleteFile(folder);
    }

    public static class Image {

        private String originalImageFileName;
        private String resizedImageFileName;

        public Image(String originalImage, String resizedImageFileName) {
            this.originalImageFileName = originalImage;
            this.resizedImageFileName = resizedImageFileName;
        }

        public String getOriginalImage() {
            return originalImageFileName;
        }

        public void setOriginalImage(String originalImage) {
            this.originalImageFileName = originalImage;
        }

        public String getResizedImageFileName() {
            return resizedImageFileName;
        }

        public void setResizedImageFileName(String resizedImageFileName) {
            this.resizedImageFileName = resizedImageFileName;
        }
    }
}
