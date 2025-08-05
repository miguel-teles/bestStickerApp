package io.github.miguelteles.beststickerapp.domain.pojo;

import com.google.gson.annotations.SerializedName;

public class ResponseAPIBase {

    private Integer status;
    @SerializedName(value = "message",
            alternate = "Message")
    private String message;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
