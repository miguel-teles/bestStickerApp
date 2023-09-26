package com.example.samplestickerapp.exception;

import android.content.Context;
import android.widget.Toast;

public abstract class StickerExceptionHandler {

    public static void handleException(StickerException ex, Context context) {
        Boolean isCritical = null;

        if (ex != null) {
            ex.printStackTrace();
        }

        StringBuilder text = new StringBuilder();
        if (ex.getStickerCriticalException() != null) {
            isCritical = true;
            text.append(ex.getStickerCriticalException());
        } else if (ex.getStickerExceptionEnum() != null) {
            isCritical = false;
            text.append(ex.getStickerExceptionEnum());
        }

        if (text.length() != 0 && ex.getMsgErro() != null) {
            text.append(" - ");
            text.append(ex.getMsgErro());
        } else if (ex.getMsgErro() != null) {
            text.append(ex.getMsgErro());
        }

        text.append(" - ");
        text.append(ex.getMethod());

        int duration = isCritical != null && isCritical || ex.getStickerDBExceptionEnum()!=null ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;

        Toast.makeText(context, text.toString(), duration).show();
    }

    public static String returnExceptionType(Exception ex) {
        if (ex instanceof NullPointerException) {
            return "Valores nulos";
        } else if (ex instanceof NumberFormatException) {
            return "Formato numérico inválido";
        } else {
            return ex.getClass().getName();
        }
    }

}
