package io.github.miguelteles.beststickerapp.exception.handler;

import android.app.AlertDialog;
import android.content.Context;

import io.github.miguelteles.beststickerapp.BuildConfig;
import io.github.miguelteles.beststickerapp.exception.StickerException;

public class StickerExceptionHandler {

    private static StickerExceptionNotifier stickerExceptionNotifier;

    private StickerExceptionHandler() {
    }

    public static void handleException(StickerException ex, Context context) {
        printStackTrace(ex);

        StringBuilder finalMessage = new StringBuilder();
        finalMessage.append(ex.getStickerExceptionEnumMessage());
        setExceptionCustomMessage(finalMessage, ex.getMsgErro());
        setExceptionCauseDetails(ex, finalMessage);

        System.out.println(ex.getLocationException());
        stickerExceptionNotifier.addExceptionToNotificationQueue(ex);
        new AlertDialog.Builder(context)
                .setTitle("Erro :(")
                .setMessage(finalMessage)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private static void setExceptionCauseDetails(StickerException ex, StringBuilder finalMessage) {
        if (ex.getException() != null) {
            finalMessage.append(" - ");
            finalMessage.append(ex.getException().getClass().getName());
            StackTraceElement[] stackTraceElement = ex.getException().getStackTrace();
            finalMessage.append(" - ");
            finalMessage.append(stackTraceElement[0].getFileName() + "(" + stackTraceElement[0].getLineNumber() + ")");
        }

    }

    private static void printStackTrace(StickerException ex) {
        if (ex.getException() != null) {
            ex.getException().printStackTrace();
        }
    }

    private static void setExceptionCustomMessage(StringBuilder finalMessage, String msgErro) {
        if (finalMessage.length() != 0 && msgErro != null) {
            finalMessage.append(" - ");
            finalMessage.append(msgErro);
        } else if (msgErro != null) {
            finalMessage.append(msgErro);
        }
    }

    public static StickerExceptionNotifier getStickerExceptionNotifier() {
        if (stickerExceptionNotifier == null) {
            if (BuildConfig.DEBUG) {
                stickerExceptionNotifier = new DebugStickerExceptionNotifier();
            } else {
                stickerExceptionNotifier = ProductionStickerExceptionNotifier.getInstance();
            }
        }
        return stickerExceptionNotifier;
    }
}
