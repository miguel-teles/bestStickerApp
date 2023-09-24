package com.example.samplestickerapp.exception;

import android.content.Context;

import com.example.samplestickerapp.exception.enums.StickerCriticalExceptionEnum;
import com.example.samplestickerapp.exception.enums.StickerDBExceptionEnum;
import com.example.samplestickerapp.exception.enums.StickerExceptionEnum;

import java.util.Date;

public class StickerException extends Exception {

    private Exception exception;
    private String msgErro;
    private StickerExceptionEnum stickerExceptionEnum;
    private StickerCriticalExceptionEnum stickerCriticalExceptionEnum;
    private StickerDBExceptionEnum stickerDBExceptionEnum;
    private Date dtException;
    private String method;

    private StickerException(Exception ex,
                             String method,
                             StickerExceptionEnum stickerExceptionEnum,
                             StickerCriticalExceptionEnum stickerCriticalExceptionEnum,
                             StickerDBExceptionEnum stickerDBExceptionEnum,
                             String msgErro) {
        super(msgErro, ex);
        this.msgErro = msgErro;
        this.stickerExceptionEnum = stickerExceptionEnum;
        this.stickerCriticalExceptionEnum = stickerCriticalExceptionEnum;
        this.stickerDBExceptionEnum = stickerDBExceptionEnum;
        this.dtException = new Date();
        this.exception = ex;
        this.method = method;
    }

    //normal exception
    public StickerException(Exception ex,
                            String method,
                            StickerExceptionEnum stickerExceptionEnum,
                            String msgError) {
        this(ex, method, stickerExceptionEnum, null, null, msgError);
    }

    //critical exception
    public StickerException(Exception ex,
                            String method,
                            StickerCriticalExceptionEnum stickerCriticalExceptionEnum,
                            String msgError) {
        this(ex, method, null, stickerCriticalExceptionEnum, null, msgError);
    }

    public StickerException(Exception ex,
                            String method,
                            StickerDBExceptionEnum stickerDBExceptionEnum,
                            String msgError) {
        this(ex, method, null, null, stickerDBExceptionEnum, msgError);
    }

    public Exception getException() {
        return exception;
    }

    public String getMsgErro() {
        return msgErro;
    }

    public StickerExceptionEnum getStickerExceptionEnum() {
        return stickerExceptionEnum;
    }

    public Date getDtException() {
        return dtException;
    }

    public StickerCriticalExceptionEnum getStickerCriticalException() {
        return stickerCriticalExceptionEnum;
    }

    public String getMethod() {
        return method;
    }


    public StickerCriticalExceptionEnum getStickerCriticalExceptionEnum() {
        return stickerCriticalExceptionEnum;
    }

    public StickerDBExceptionEnum getStickerDBExceptionEnum() {
        return stickerDBExceptionEnum;
    }
}
