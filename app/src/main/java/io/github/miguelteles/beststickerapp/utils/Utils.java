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
    private static Context applicationContext;
    private static Gson gson;

    public static Gson getGson() {
        if (gson == null) {
            gson = new Gson();
        }
        return gson;
    }

    public static boolean isNothing(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean isNothing(List<?> list) {
        return list == null || list.isEmpty();
    }

    public static boolean isNothing(Editable editable) {
        return editable == null || editable.toString() == null || editable.toString().trim().isEmpty();
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
