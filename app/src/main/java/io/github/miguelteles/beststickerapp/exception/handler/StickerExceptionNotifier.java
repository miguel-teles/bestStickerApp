package io.github.miguelteles.beststickerapp.exception.handler;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.github.miguelteles.beststickerapp.domain.pojo.ResponseAPIBase;
import io.github.miguelteles.beststickerapp.exception.StickerFolderException;
import io.github.miguelteles.beststickerapp.services.FoldersManagementService;
import io.github.miguelteles.beststickerapp.services.client.ExceptionNotifierAPI;
import io.github.miguelteles.beststickerapp.utils.Utils;

public class StickerExceptionNotifier {

    private static StickerExceptionNotifier instance;
    private final FoldersManagementService foldersManagementService;
    private final ExceptionNotifierAPI exceptionNotifierAPI;
    private final ConcurrentLinkedQueue<File> notificationQueue;
    private final Executor executor;

    private StickerExceptionNotifier() {
        this.foldersManagementService = FoldersManagementService.getInstance();
        this.exceptionNotifierAPI = new ExceptionNotifierAPI();
        this.notificationQueue = new ConcurrentLinkedQueue<>();
        if (foldersManagementService.getErrosFolder().listFiles() != null) {
            this.notificationQueue.addAll(Arrays.asList(foldersManagementService.getErrosFolder().listFiles()));
        }
        this.executor = Executors.newSingleThreadExecutor();
    }

    public static StickerExceptionNotifier getInstance() {
        if (instance == null) {
            instance = new StickerExceptionNotifier();
        }
        return instance;
    }


    public void initNotifying() {
        executor.execute(() -> {
            System.out.println("Init new thread stickerExceptionNotifier");
            if (notificationQueue != null) {
                for (File exceptionLog : notificationQueue) {
                    try {
                        String notification = String.join("", Files.readAllLines(exceptionLog.toPath()));

                        System.out.println("Sending log " + exceptionLog.getName());
                        ResponseAPIBase post = exceptionNotifierAPI.post(notification);
                        if (post.getStatus() == 200) {
                            System.out.println("Log sent!");
                            foldersManagementService.deleteFile(exceptionLog);
                            notificationQueue.poll();
                            System.out.println("Log removed from queue and device");
                        }
                    } catch (Exception e) {
                        addExceptionToNotificationQueue(e);
                    }
                }
            }
        });
    }

    public void addExceptionToNotificationQueue(Exception exception) {
        ExceptionNotifierRQ notification = buildNotificationFromException(exception);

        File notificationLogFile = new File(foldersManagementService.getErrosFolder(), Utils.formatData(new Date(), "yyyyMMddHHmmss"));
        try (OutputStream out = new FileOutputStream(notificationLogFile);
             InputStream in = new ByteArrayInputStream(Utils.getGson().toJson(notification).getBytes(StandardCharsets.UTF_8))) {

            out.write(foldersManagementService.readBytesFromInputStream(in));

            notificationQueue.add(notificationLogFile);
        } catch (IOException | StickerFolderException ignored) {
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

    private static void addExceptionInfo(Throwable throwable, StringBuilder notificationText) {
        notificationText.append("Exception type: ").append(throwable.getClass().getName()).append("\n");
        notificationText.append("Message: ").append(throwable.getMessage()).append("\n");
        notificationText.append("Stack trace: ").append(Arrays.toString(throwable.getStackTrace())).append("\n");
        if (throwable.getCause() != null) {
            notificationText.append("-> Cause ").append("\n");
            addExceptionInfo(throwable.getCause(), notificationText);
        }
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
