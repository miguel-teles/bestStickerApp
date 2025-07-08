package com.example.samplestickerapp.exception;

import com.example.samplestickerapp.exception.enums.StickerFolderExceptionEnum;
import com.example.samplestickerapp.exception.enums.StickerDataBaseExceptionEnum;
import com.example.samplestickerapp.exception.enums.StickerExceptionEnum;

import java.util.Date;

public class StickerException extends RuntimeException {

    private Exception exception;
    private String msgErro;
    private String dsErro;
    private StickerExceptionEnum stickerExceptionEnum;
    private Date dtException;
    private String locationException;

    public StickerException(Exception ex,
                            StickerExceptionEnum stickerExceptionEnum,
                            String msgErro) {
        super(msgErro, ex);
        this.msgErro = msgErro;
        this.stickerExceptionEnum = stickerExceptionEnum;
        this.dtException = new Date();
        this.exception = ex;
        StackTraceElement stackTraceElement = null;
        if (ex != null) {
            ex.printStackTrace();
            stackTraceElement = ex.getStackTrace()[0];
        } else {
            stackTraceElement = getStackTrace()[0];
        }
        this.locationException = stackTraceElement.getClassName() + "." + stackTraceElement.getMethodName() + ":" + stackTraceElement.getLineNumber();
    }

    public Exception getException() {
        return exception;
    }

    public String getMsgErro() {
        return msgErro;
    }

    public String getStickerExceptionEnumMessage() {
        if (stickerExceptionEnum != null) {
            return stickerExceptionEnum.toString();
        }
        return "";
    }

    public Date getDtException() {
        return dtException;
    }

    public String getLocationException() {
        return locationException;
    }

}
