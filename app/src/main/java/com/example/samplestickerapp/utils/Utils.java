package com.example.samplestickerapp.utils;

import android.content.Context;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class Utils {

    public static int PICK_IMAGE_REQUEST_CODE = 1;
    public static String contentsPath = "Android/app/src/main/assets/contents.json";

    public static Gson configuraGson() {
        return new Gson(); //confirugrar aqui se precisar depois
    }

    public static String readFile(Context context, String filePath){
        String fileContent=null;
        try (BufferedReader bf = new BufferedReader(new FileReader(new File(filePath)))){

            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line=bf.readLine())!=null){
                stringBuilder.append(line);
            }

            fileContent = new String(stringBuilder);
        } catch (IOException iob){
            Toast.makeText(context, "Erro ao buscar pasta de figurinhas", Toast.LENGTH_SHORT).show();
        }
        return fileContent;
    }

    public static Integer encontraMaior(List<Number> numberList){
        Integer maiorNmb = null;
        for (Number nmb : numberList){
            if (maiorNmb==null) {
                maiorNmb = nmb.intValue();
            } else {
                int dbNmb = nmb.intValue();
                if (dbNmb>maiorNmb){
                    maiorNmb=dbNmb;
                }
            }
        }
        return maiorNmb;
    }

    public static void copyFile(String sourcePath, String destinationPath){
        FileInputStream inputStream=null;
        FileOutputStream outputStream=null;
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
}
