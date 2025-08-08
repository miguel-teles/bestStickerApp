package io.github.miguelteles.beststickerapp.services.client;

import android.content.Context;

import java.io.IOException;

import io.github.miguelteles.beststickerapp.BuildConfig;
import io.github.miguelteles.beststickerapp.domain.pojo.ResponseAPIAppLatestVersion;
import io.github.miguelteles.beststickerapp.domain.pojo.ResponseAPIGetDownloadAppUrl;
import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.exception.StickerFatalErrorException;
import io.github.miguelteles.beststickerapp.exception.StickerWebCommunicationException;
import io.github.miguelteles.beststickerapp.exception.enums.StickerWebCommunicationExceptionEnum;
import io.github.miguelteles.beststickerapp.services.client.interfaces.GetDownloadAppUrlAPI;
import io.github.miguelteles.beststickerapp.utils.Utils;
import okhttp3.Call;
import okhttp3.Response;

public class GetDownloadAppUrlAPIImpl extends HttpClient implements GetDownloadAppUrlAPI {

    public GetDownloadAppUrlAPIImpl(Context context) throws StickerFatalErrorException {
        super(BuildConfig.API_ENDPOINT, context);
    }

    @Override
    public ResponseAPIGetDownloadAppUrl getDownloadAppUrl(String versionName) throws StickerException {
        Call call = this.get("/download-link?versionName=" + versionName);
        try (Response response = call.execute()) {
            return Utils.getGson().fromJson(response.body().string(), ResponseAPIGetDownloadAppUrl.class);
        } catch (IOException ex) {
            throw new StickerWebCommunicationException(ex, StickerWebCommunicationExceptionEnum.POST, "Erro ao buscar link de download da nova versão.");
        }
    }
}
