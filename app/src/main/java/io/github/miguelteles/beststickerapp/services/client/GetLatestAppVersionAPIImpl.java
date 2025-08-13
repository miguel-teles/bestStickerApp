package io.github.miguelteles.beststickerapp.services.client;

import android.content.Context;

import java.io.IOException;
import java.net.SocketTimeoutException;

import io.github.miguelteles.beststickerapp.BuildConfig;
import io.github.miguelteles.beststickerapp.domain.pojo.ResponseAPIAppLatestVersion;
import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.exception.StickerFatalErrorException;
import io.github.miguelteles.beststickerapp.exception.StickerWebCommunicationException;
import io.github.miguelteles.beststickerapp.exception.enums.StickerWebCommunicationExceptionEnum;
import io.github.miguelteles.beststickerapp.services.client.interfaces.GetLatestAppVersionAPI;
import io.github.miguelteles.beststickerapp.utils.Utils;
import okhttp3.Call;
import okhttp3.Response;

public class GetLatestAppVersionAPIImpl extends HttpClient implements GetLatestAppVersionAPI {

    public GetLatestAppVersionAPIImpl(Context context) throws StickerFatalErrorException {
        super(BuildConfig.API_ENDPOINT, context);
    }

    @Override
    public ResponseAPIAppLatestVersion get() throws StickerException {
        Call call = this.get("/latest-version");
        try (Response response = call.execute()) {
            return Utils.getGson().fromJson(response.body().string(), ResponseAPIAppLatestVersion.class);
        } catch (SocketTimeoutException ex) {
            throw new StickerWebCommunicationException(ex, StickerWebCommunicationExceptionEnum.TIMEOUT, "/latest-version");
        }  catch (IOException ex) {
            throw new StickerWebCommunicationException(ex, StickerWebCommunicationExceptionEnum.POST, "Erro ao checkar por uma nova versão do aplicativo.");
        }
    }
}
