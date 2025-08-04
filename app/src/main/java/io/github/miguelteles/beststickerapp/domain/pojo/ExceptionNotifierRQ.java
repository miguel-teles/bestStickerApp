package io.github.miguelteles.beststickerapp.domain.pojo;

import java.util.Arrays;
import java.util.Date;

import io.github.miguelteles.beststickerapp.utils.Utils;

public class ExceptionNotifierRQ {

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