package com.example.samplestickerapp.exception;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

public abstract class StickerExceptionHandler {

    public static void handleException(StickerException ex, Context context) {
        Boolean isCritical = null;

        if (ex != null) {
            ex.getException().printStackTrace();
        }

        StringBuilder text = new StringBuilder();
        if (ex.getStickerCriticalException() != null) {
            isCritical = true;
            text.append(ex.getStickerCriticalException().toString());
        } else if (ex.getStickerExceptionEnum() != null) {
            isCritical = false;
            text.append(ex.getStickerExceptionEnum().toString());
        }

        if (text.length() != 0 && ex.getMsgErro() != null) {
            text.append(" - ");
            text.append(ex.getMsgErro());
        } else if (ex.getMsgErro() != null) {
            text.append(ex.getMsgErro());
        }
        text.append(" - ");
        text.append(ex.getException().getClass().getName());

        StackTraceElement[] stackTraceElement = ex.getStackTrace();
        if (stackTraceElement != null) {
            text.append(" - ");
            text.append(stackTraceElement[0].getFileName() + "(" + stackTraceElement[0].getLineNumber() + ")");
        }

        System.out.println(ex.getLocationException());
        new AlertDialog.Builder(context)
                .setTitle("Erro :(")
                .setMessage(text)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
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
