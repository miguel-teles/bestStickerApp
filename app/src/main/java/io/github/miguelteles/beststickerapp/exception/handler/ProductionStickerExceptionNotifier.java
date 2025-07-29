package io.github.miguelteles.beststickerapp.exception.handler;

import android.net.Uri;

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
        this.exceptionNotifierAPI = new ExceptionNotifierAPIImpl();
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
            System.out.println("Init new thread stickerExceptionNotifier");
            try {
                this.notificationQueue.addAll(resourcesManagement.getFilesFromDirectory(resourcesManagement.getOrCreateLogErrorsDirectory()));
                List<Uri> notificationQueueSnapshot = notificationQueue.stream().collect(Collectors.toList());
                for (Uri exceptionLog : notificationQueueSnapshot) {

                    String notification = resourcesManagement.getContentAsString(exceptionLog);

                    System.out.println("Sending log " + exceptionLog.getLastPathSegment());
                    ResponseAPIBase post = exceptionNotifierAPI.post(notification);
                    if (isNotificationSuccessfullySent(post)) {
                        System.out.println("Log sent!");
                        resourcesManagement.deleteFile(exceptionLog);
                        notificationQueue.poll();
                        System.out.println("Log removed from queue and device");
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
                .build();
    }
    public static class ExceptionNotifierRQ {

        private String key;
        private String exceptionType;
        private String message;
        private String stacktrace;
        private ExceptionNotifierRQ cause;

        public void setExceptionType(String exceptionType) {
            this.exceptionType = exceptionType;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public void setStacktrace(String stacktrace) {
            this.stacktrace = stacktrace;
        }

        public void setCause(ExceptionNotifierRQ cause) {
            this.cause = cause;
        }

        public String getExceptionType() {
            return exceptionType;
        }

        public String getMessage() {
            return message;
        }

        public String getStacktrace() {
            return stacktrace;
        }

        public ExceptionNotifierRQ getCause() {
            return cause;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public static class RQBuilder {

            private ExceptionNotifierRQ exceptionNotifierRQ;
            private Throwable throwable;

            public static RQBuilder builder(Throwable throwable) {
                return new RQBuilder(throwable);
            }

            public ExceptionNotifierRQ build() {
                return exceptionNotifierRQ;
            }

            private RQBuilder(Throwable throwable) {
                if (throwable != null) {
                    this.exceptionNotifierRQ = new ExceptionNotifierRQ();
                    this.throwable = throwable;
                }
            }

            public RQBuilder setExceptionType() {
                if (throwable != null) {
                    this.exceptionNotifierRQ.setExceptionType(throwable.getClass().getName());
                }
                return this;
            }

            public RQBuilder setMessage() {
                if (throwable != null) {
                    this.exceptionNotifierRQ.setMessage(throwable.getMessage());
                }
                return this;
            }

            public RQBuilder setStackTrace() {
                if (throwable != null) {
                    this.exceptionNotifierRQ.setStacktrace(Arrays.toString(throwable.getStackTrace()));
                }
                return this;
            }

            public RQBuilder setKey() {
                if (throwable != null) {
                    this.exceptionNotifierRQ.setKey(Utils.formatData(new Date(), "yyyyMMddHHmmss"));
                }
                return this;
            }

            public RQBuilder setCause() {
                if (throwable != null) {
                    this.exceptionNotifierRQ.setCause(RQBuilder.builder(throwable.getCause())
                            .setKey()
                            .setMessage()
                            .setExceptionType()
                            .setStackTrace()
                            .setCause()
                            .build());
                }
                return this;
            }
        }
    }


}
