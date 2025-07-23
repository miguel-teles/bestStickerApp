package io.github.miguelteles.beststickerapp.services.client;

import java.io.IOException;

import io.github.miguelteles.beststickerapp.domain.pojo.ResponseAPIAppLatestVersion;
import io.github.miguelteles.beststickerapp.exception.StickerHttpClientException;
import io.github.miguelteles.beststickerapp.exception.enums.StickerHttpClientExceptionEnum;
import io.github.miguelteles.beststickerapp.services.client.interfaces.GetLatestAppVersionAPI;
import io.github.miguelteles.beststickerapp.utils.Utils;
import okhttp3.Call;
import okhttp3.Response;

public class GetLatestAppVersionImpl extends HttpClient implements GetLatestAppVersionAPI {

    public GetLatestAppVersionImpl() {
        super("http://192.168.68.103:8081/beststickerapp");
    }

    @Override
    public ResponseAPIAppLatestVersion get() throws StickerHttpClientException {
        Call call = this.get("/latest-version");
        try (Response response = call.execute()) {
            return Utils.getGson().fromJson(response.body().string(), ResponseAPIAppLatestVersion.class);
        } catch (IOException ex) {
            throw new StickerHttpClientException(ex, StickerHttpClientExceptionEnum.POST, "Erro ao checkar por uma nova versão do aplicativo.");
        }
    }
}
