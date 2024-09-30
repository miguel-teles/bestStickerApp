package com.example.samplestickerapp.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Picture;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import com.example.samplestickerapp.exception.StickerException;
import com.example.samplestickerapp.exception.enums.StickerCriticalExceptionEnum;
import com.example.samplestickerapp.exception.enums.StickerDBExceptionEnum;
import com.example.samplestickerapp.exception.enums.StickerExceptionEnum;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

abstract public class Folders {

    public static final int TRAY_IMAGE_MAX_FILE_SIZE = 50; //50KB
    public static final int STICKER_IMAGE_MAX_FILE_SIZE = 100; //50KB
    public static final int TRAY_IMAGE_SIZE = 96; //96pxs
    public static final int STICKER_IMAGE_SIZE = 512; //512pxs

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

    public static File makeDirPackIdentifier(String stickerPackFolderName, Context context) throws StickerException {
        try {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

                File externalDir = context.getExternalFilesDir(null);

                File folderPacks = new File(externalDir, DirectoryNames.PACKS);
                if (folderPacks.exists()) {
                    File novoPacotePasta = new File(folderPacks, stickerPackFolderName);
                    if (!novoPacotePasta.mkdir()) {
                        throw new StickerException(null, StickerCriticalExceptionEnum.CREATE_FOLDER_PACOTE, null);
                    }
                    return novoPacotePasta;
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

    /**
     * @param folderPack    The sticker pack folder where all the images are
     * @param sourceImgPath The image being copied
     * @param imageFileName The file's name created
     * @return Retorna as imagens copiadas da imagem original. A primeira é a imagem original e a segunda é a imagem reduzida
     **/
    public static File[] copiaFotoParaPastaPacote(String folderPack,
                                                  String sourceImgPath,
                                                  String imageFileName,
                                                  int imgSize,
                                                  int fileSize,
                                                  Context context) throws StickerException {
        try {
            File pacotePasta = getPackFolderByFolderName(folderPack, context);
            try {
                //Copia a imagem original para a pasta das figurinhas. O resultado gera 2 imagens, uma com o tamanho original e a outra com o tamanho reduzido
                File img = new File(sourceImgPath);
                File packImg = new File(imageFileName + getFileExtension(img, true)); //imagem
                File packImgRzd = new File(imageFileName + "Rzd" + getFileExtension(img, true)); //imagem pequena
                File absolutePackImg = new File(pacotePasta, packImg.getPath()); //file absoluta da imagem
                File absolutePackImgRzd = new File(pacotePasta, packImgRzd.getPath()); //file absoluta da imagem pequena
                copiaImagem(img, absolutePackImg);
                copiaImagem(img, absolutePackImgRzd);

                resizeImage(absolutePackImgRzd, imgSize, fileSize);

                return new File[]{packImg, packImgRzd};
            } catch (StickerException ste) {
                throw ste;
            } catch (Exception ex) {
                throw new StickerException(ex, StickerCriticalExceptionEnum.COPY, "Erro ao copiar o arquivo da foto do pacote para a pasta dele");
            }
        } catch (StickerException ste) {
            throw ste;
        } catch (Exception ex) {
            throw new StickerException(ex, StickerExceptionEnum.CSP, "Erro ao copiar foto do pacote para a pasta do pacote " + folderPack);
        }
    }

    private static void copiaImagem(File sourceFile, File destinationFile) throws StickerException {
        try {
            destinationFile.setWritable(true);
            if (!destinationFile.createNewFile()) {
                throw new StickerException(null, StickerCriticalExceptionEnum.COPY, "Imagem não criada");
            }
            FileInputStream imageFileInputStream = new FileInputStream(sourceFile);
            FileOutputStream imageFileOutputStream = new FileOutputStream(destinationFile);

            FileChannel sourceImageChannel = imageFileInputStream.getChannel();
            FileChannel destinationImageChannel = imageFileOutputStream.getChannel();

            destinationImageChannel.transferFrom(sourceImageChannel, 0, sourceImageChannel.size());

            destinationImageChannel.close();
            imageFileOutputStream.close();
            sourceImageChannel.close();
            imageFileInputStream.close();
        } catch (Exception ex) {
            throw new StickerException(ex, StickerCriticalExceptionEnum.COPY, "Erro ao copiar file");
        }
    }

    private static void resizeImage(File packImg,
                                    int imgSize,
                                    int fileSize) throws StickerException {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(packImg.getAbsolutePath());
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, imgSize, imgSize, false);
            FileOutputStream out = new FileOutputStream(packImg);
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception ex) {
            throw new StickerException(ex, StickerCriticalExceptionEnum.RESIZE, "Imagem: " + packImg.getName());
        }
    }

    public static void deleteStickerPackFolder(File stickerPackFolderName, Context context) throws StickerException {
        try {
            stickerPackFolderName.deleteOnExit();
        } catch (Exception ex) {
            throw new StickerException(ex, StickerCriticalExceptionEnum.DELETE_FOLDER, "Pasta: " + stickerPackFolderName);
        }
    }
}
