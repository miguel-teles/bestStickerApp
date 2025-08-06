package io.github.miguelteles.beststickerapp.exception.handler;

import android.net.Uri;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import io.github.miguelteles.beststickerapp.domain.pojo.ExceptionNotifierRQ;
import io.github.miguelteles.beststickerapp.domain.pojo.ResponseAPIBase;
import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.exception.StickerFolderException;
import io.github.miguelteles.beststickerapp.services.FileResourceManagement;
import io.github.miguelteles.beststickerapp.services.client.ExceptionNotifierAPIImpl;
import io.github.miguelteles.beststickerapp.services.client.interfaces.ExceptionNotifierAPI;
import io.github.miguelteles.beststickerapp.services.interfaces.ResourcesManagement;
import io.github.miguelteles.beststickerapp.utils.Utils;

public class ProductionStickerExceptionNotifier implements StickerExceptionNotifier {

    private static ProductionStickerExceptionNotifier instance;
    private final ResourcesManagement resourcesManagement;
    private final ExceptionNotifierAPI exceptionNotifierAPI;
    private final ConcurrentLinkedQueue<Uri> notificationQueue;
    private final Executor executor;

    protected ProductionStickerExceptionNotifier() throws StickerException {
        this.resourcesManagement = FileResourceManagement.getInstance();
        this.exceptionNotifierAPI = new ExceptionNotifierAPIImpl(Utils.getApplicationContext());
        this.notificationQueue = new ConcurrentLinkedQueue<>();
        this.executor = Executors.newSingleThreadExecutor();
    }

    public static ProductionStickerExceptionNotifier getInstance() throws StickerException {
        if (instance == null) {
            instance = new ProductionStickerExceptionNotifier();
        }
        return instance;
    }


    public void initNotifying() {
        executor.execute(() -> {
            Log.i("TAG","Init new thread stickerExceptionNotifier");
            try {
                this.notificationQueue.addAll(resourcesManagement.getFilesFromDirectory(resourcesManagement.getOrCreateLogErrorsDirectory()));
                List<Uri> notificationQueueSnapshot = notificationQueue.stream().collect(Collectors.toList());
                for (Uri exceptionLog : notificationQueueSnapshot) {

                    String notification = resourcesManagement.getContentAsString(exceptionLog);

                    Log.i("TAG", "Sending log " + exceptionLog.getLastPathSegment());
                    ResponseAPIBase post = exceptionNotifierAPI.post(notification);
                    if (isNotificationSuccessfullySent(post)) {
                        Log.i("TAG", "Log sent!");
                        resourcesManagement.deleteFile(exceptionLog);
                        notificationQueue.poll();
                        Log.i("TAG", "Log removed from queue and device");
                    }

                }

            } catch (Exception e) {
                writeExceptionIntoLogFile(e);
            }
        });
    }

    private static boolean isNotificationSuccessfullySent(ResponseAPIBase post) {
        return post.getStatus() == null;
    }

    public void writeExceptionIntoLogFile(Throwable exception) {
        ExceptionNotifierRQ notification = buildNotificationFromException(exception);

        try {
            Uri notificationLogFile = resourcesManagement.getOrCreateFile(resourcesManagement.getOrCreateLogErrorsDirectory(), Utils.formatData(new Date(), "yyyyMMddHHmmss"));

            InputStream in = new ByteArrayInputStream(Utils.getGson().toJson(notification).getBytes(StandardCharsets.UTF_8));
            resourcesManagement.writeToFile(notificationLogFile, in);

        } catch (StickerFolderException ignored) {
        }
    }

    private ExceptionNotifierRQ buildNotificationFromException(Throwable throwable) {
        return ExceptionNotifierRQ.RQBuilder.builder(throwable)
                .setKey()
                .setExceptionType()
                .setCause()
                .setMessage()
                .setStackTrace()
                .setAppVersion()
                .build();
    }

}
