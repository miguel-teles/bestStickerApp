package io.github.miguelteles.beststickerapp.services.client;

import android.content.Context;

import java.io.IOException;

import io.github.miguelteles.beststickerapp.domain.pojo.ResponseAPIAppLatestVersion;
import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.exception.StickerFatalErrorException;
import io.github.miguelteles.beststickerapp.exception.StickerHttpClientException;
import io.github.miguelteles.beststickerapp.exception.enums.StickerHttpClientExceptionEnum;
import io.github.miguelteles.beststickerapp.services.client.interfaces.GetLatestAppVersionAPI;
import io.github.miguelteles.beststickerapp.utils.Utils;
import okhttp3.Call;
import okhttp3.Response;

public class GetLatestAppVersionAPIImpl extends HttpClient implements GetLatestAppVersionAPI {

    public GetLatestAppVersionAPIImpl(Context context) throws StickerFatalErrorException {
        super("http://192.168.68.103:8081/beststickerapp", context);
    }

    @Override
    public ResponseAPIAppLatestVersion get() throws StickerException {
        Call call = this.get("/latest-version");
        try (Response response = call.execute()) {
            return Utils.getGson().fromJson(response.body().string(), ResponseAPIAppLatestVersion.class);
        } catch (IOException ex) {
            throw new StickerHttpClientException(ex, StickerHttpClientExceptionEnum.POST, "Erro ao checkar por uma nova versão do aplicativo.");
        }
    }
}
