package io.github.miguelteles.beststickerapp.services.client;

import com.google.gson.Gson;

import java.io.IOException;

import io.github.miguelteles.beststickerapp.BuildConfig;
import io.github.miguelteles.beststickerapp.domain.pojo.ResponseAPIBase;
import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.exception.StickerFatalErrorException;
import io.github.miguelteles.beststickerapp.exception.StickerHttpClientException;
import io.github.miguelteles.beststickerapp.exception.enums.StickerHttpClientExceptionEnum;
import io.github.miguelteles.beststickerapp.services.client.interfaces.ExceptionNotifierAPI;
import okhttp3.Call;
import okhttp3.Response;

public class ExceptionNotifierAPIImpl extends HttpClient implements ExceptionNotifierAPI {

    private final Gson gson;

    public ExceptionNotifierAPIImpl() throws StickerException {
        super(BuildConfig.API_ENDPOINT);
        gson = new Gson();
    }

    public ResponseAPIBase post(String exceptionNotification) throws StickerException {
        Call call = this.post("/exception-notification", exceptionNotification);
        try (Response response = call.execute()) {
            return gson.fromJson(response.body().string(), ResponseAPIBase.class);
        } catch (IOException ex) {
            throw new StickerHttpClientException(ex, StickerHttpClientExceptionEnum.POST, "Erro ao notificar erro no aplicativo");
        }
    }
}