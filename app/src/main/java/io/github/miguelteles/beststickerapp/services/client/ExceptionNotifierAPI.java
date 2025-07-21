package io.github.miguelteles.beststickerapp.services.client;

import com.google.gson.Gson;

import java.io.IOException;

import io.github.miguelteles.beststickerapp.BuildConfig;
import io.github.miguelteles.beststickerapp.domain.pojo.ResponseAPIBase;
import io.github.miguelteles.beststickerapp.exception.StickerHttpClientException;
import io.github.miguelteles.beststickerapp.exception.enums.StickerHttpClientExceptionEnum;
import okhttp3.Call;
import okhttp3.Response;

public class ExceptionNotifierAPI extends HttpClient {

    private final Gson gson;

    public ExceptionNotifierAPI() {
        super(BuildConfig.API_PRODUCTION_URL);
        gson = new Gson();
    }

    public ResponseAPIBase post(String exceptionNotification) throws StickerHttpClientException {
        Call call = this.post("/exception-notify", gson.toJson(new ExceptionNotificationRQ(exceptionNotification)));
        try (Response response = call.execute()) {
            return gson.fromJson(response.body().string(), ResponseAPIBase.class);
        } catch (IOException ex) {
            throw new StickerHttpClientException(ex, StickerHttpClientExceptionEnum.POST, "Error converting image to webp via web service");
        }
    }

    record ExceptionNotificationRQ(String exception) {
    }
}