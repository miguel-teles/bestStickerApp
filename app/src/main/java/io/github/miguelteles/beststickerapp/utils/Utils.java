package io.github.miguelteles.beststickerapp.utils;

import android.content.Context;
import android.text.Editable;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Utils {

    public static int PICK_IMAGE_REQUEST_CODE = 1;
    public static String tpAmbiente = "H";
    private static Context applicationContext;
    private static Gson gson;

    public static Gson getGson() {
        if (gson == null) {
            gson = new Gson();
        }
        return gson;
    }

    public static Integer encontraMaior(List<Number> numberList) {
        Integer maiorNmb = null;
        for (Number nmb : numberList) {
            if (maiorNmb == null) {
                maiorNmb = nmb.intValue();
            } else {
                int dbNmb = nmb.intValue();
                if (dbNmb > maiorNmb) {
                    maiorNmb = dbNmb;
                }
            }
        }
        return maiorNmb;
    }

    public static boolean isNothing(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean isNothing(Editable editable) {
        return editable == null || editable.toString() == null || editable.toString().trim().isEmpty();
    }

    public static void copyFile(String sourcePath, String destinationPath) {
        FileInputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            File sourceFile = new File(sourcePath);
            File destinationFile = new File(destinationPath);

            inputStream = new FileInputStream(sourceFile);
            outputStream = new FileOutputStream(destinationFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            System.out.println("Image copied successfully.");
        } catch (IOException e) {
            System.out.println("Failed to copy the image: " + e.getMessage());
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                System.out.println("Failed to close the streams: " + e.getMessage());
            }
        }
    }

    public static String formatData(Date date, String format) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        return simpleDateFormat.format(date);
    }

    public static Context getApplicationContext() {
        return applicationContext;
    }

    public static void setApplicationContext(Context applicationContext) {
        if (Utils.applicationContext == null) {
            Utils.applicationContext = applicationContext;
        }
    }
}
