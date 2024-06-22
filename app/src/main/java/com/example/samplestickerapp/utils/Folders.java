package com.example.samplestickerapp.utils;

import android.content.Context;
import android.os.Environment;

import com.example.samplestickerapp.exception.StickerException;
import com.example.samplestickerapp.exception.enums.StickerCriticalExceptionEnum;
import com.example.samplestickerapp.exception.enums.StickerExceptionEnum;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

abstract public class Folders {


    public static String getLogsFolderPath(Context context) throws StickerException {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

            File externalDir = context.getExternalFilesDir(null);
            File logs = new File(externalDir, DirectoryNames.LOGS);
            if (logs.exists()) {
                return logs.getPath();
            } else {
                throw new StickerException(null, "getLogsFolderPath", StickerCriticalExceptionEnum.GET_PATH, "Pasta de logs não encontrada");
            }

        } else {
            throw new StickerException(null, "getLogsFolderPath", StickerCriticalExceptionEnum.GET_PATH, "Erro ao buscar pasta de logs");
        }
    }

    public static String getPacksFolderPath(Context context) throws StickerException {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

            File externalDir = context.getExternalFilesDir(null);
            File packs = new File(externalDir, DirectoryNames.PACKS);
            if (packs.exists()) {
                return packs.getPath();
            } else {
                throw new StickerException(null, "getPacksFolderPath", StickerCriticalExceptionEnum.GET_PATH, "Pasta de pacotes não encontrada");
            }

        } else {
            throw new StickerException(null, "getPacksFolderPath", StickerCriticalExceptionEnum.GET_PATH, "Erro ao buscar pasta de pacotes");
        }
    }

    public static String getPackFolderPathByIdentifier(String identifier, Context context) throws StickerException {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

            File externalDir = context.getExternalFilesDir(null);
            File packs = new File(externalDir, DirectoryNames.PACKS);
            if (packs.exists()) {

                File stickerPack = new File(packs, identifier);
                if (stickerPack.exists()) {
                    return stickerPack.getPath();
                } else {
                    throw new StickerException(null, "getPacksFolderPath", StickerCriticalExceptionEnum.GET_PATH, "Pasta do pacote " + identifier + " não encontrada");
                }

            } else {
                throw new StickerException(null, "getPacksFolderPath", StickerCriticalExceptionEnum.GET_PATH, "Pasta de pacotes não encontrada");
            }

        } else {
            throw new StickerException(null, "getPacksFolderPath", StickerCriticalExceptionEnum.GET_PATH, "Erro ao buscar pasta de pacotes");
        }
    }

    public static void makeAllDirs(Context context) throws StickerException {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            makeDirPacks(context);
            makeDirLogs(context);
            makeDirErrorsLogs(context);
            makeDirCriticalErrorsLogs(context);
        } else {
            throw new StickerException(null, "makeAllDirs", StickerCriticalExceptionEnum.MKDIR_ROOT, null);
        }
    }

    public static void makeDirPackIdentifier(String identifier, Context context) throws StickerException {
        try {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

                File externalDir = context.getExternalFilesDir(null);

                File folderPacks = new File(externalDir, DirectoryNames.PACKS);
                if (folderPacks.exists()) {
                    File novoPacotePasta = new File(folderPacks, identifier);
                    if (!novoPacotePasta.mkdir()){
                        throw new StickerException(null, "makeDirPackIdentifier", StickerCriticalExceptionEnum.CREATE_FOLDER_PACOTE, null);
                    }
                } else {
                    throw new StickerException(null, "makeDirPackIdentifier", StickerCriticalExceptionEnum.GET_FOLDER, "Pasta dos pacotes não existe!");
                }

            } else {
                throw new StickerException(null, "makeDirPackIdentifier", StickerCriticalExceptionEnum.MKDIR_PACKS, null);
            }
        } catch (Exception ex) {
            throw new StickerException(ex, "makeDirPackIdentifier", StickerCriticalExceptionEnum.MKDIR_PACKS, "Erro ao criar pasta do pacote " + identifier);
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
            throw new StickerException(null, "makeDirPacks", StickerCriticalExceptionEnum.MKDIR_PACKS, null);
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
            throw new StickerException(null, "makeDirLogs", StickerCriticalExceptionEnum.MKDIR_LOG, null);
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
            throw new StickerException(null, "makeDirErrorsLogs", StickerCriticalExceptionEnum.MKDIR_LOG_ERRORS, null);
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
            throw new StickerException(null, "makeDirErrorsLogs", StickerCriticalExceptionEnum.MKDIR_LOG_CRITICAL_ERRORS, null);
        }
    }

    public static File copiaFotoParaPastaPacote(String identifier, String imgPath, Context context) throws StickerException {
        try {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

                File externalDir = context.getExternalFilesDir(null);

                File folderPacks = new File(externalDir, DirectoryNames.PACKS);
                if (folderPacks.exists()) {
                    File pacotePasta = new File(folderPacks, identifier);
                    if (pacotePasta.exists()){
                        try {
                            FileInputStream fileInputStream = new FileInputStream(imgPath);
                            FileOutputStream fileOutputStream = new FileOutputStream(pacotePasta);

                            FileChannel sourceChannel = fileInputStream.getChannel();
                            FileChannel destinationChannel = fileOutputStream.getChannel();

                            sourceChannel.transferFrom(destinationChannel, 0, sourceChannel.size());

                            sourceChannel.close();
                            destinationChannel.close();
                            fileInputStream.close();
                            fileInputStream.close();

                            return new File(pacotePasta, new File(imgPath).getName());
                        } catch (Exception ex) {
                            throw new StickerException(ex, "copiaFotoParaPastaPacote", StickerCriticalExceptionEnum.COPY, "Erro ao copiar o arquivo da foto do pacote para a pasta dele");
                        }
                    } else {
                        throw new StickerException(null, "copiaFotoParaPastaPacote", StickerCriticalExceptionEnum.GET_FOLDER, "Pasta do pacote " + identifier + " não existe!");
                    }
                } else {
                    throw new StickerException(null, "copiaFotoParaPastaPacote", StickerCriticalExceptionEnum.GET_FOLDER, "Pasta dos pacotes não existe!");
                }

            } else {
                throw new StickerException(null, "copiaFotoParaPastaPacote", StickerCriticalExceptionEnum.MKDIR_PACKS, null);
            }
        } catch (Exception ex) {
            throw new StickerException(ex, "copiaFotoParaPastaPacote", StickerExceptionEnum.CSP, "Erro ao copiar foto do pacote para a pasta do pacote " + identifier);
        }
    }
}
