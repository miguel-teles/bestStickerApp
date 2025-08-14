package io.github.miguelteles.beststickerapp.services.client;

import android.content.Context;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.SocketTimeoutException;

import io.github.miguelteles.beststickerapp.BuildConfig;
import io.github.miguelteles.beststickerapp.domain.pojo.ConvertImageToWebpRQ;
import io.github.miguelteles.beststickerapp.domain.pojo.ResponseAPIConvertedWebp;
import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.exception.StickerFatalErrorException;
import io.github.miguelteles.beststickerapp.exception.StickerWebCommunicationException;
import io.github.miguelteles.beststickerapp.exception.enums.StickerWebCommunicationExceptionEnum;
import io.github.miguelteles.beststickerapp.services.client.interfaces.ImageConverterWebpAPI;
import okhttp3.Call;
import okhttp3.Response;

public class ImageConverterWebpAPIImpl extends HttpClient implements ImageConverterWebpAPI {

    private final Gson gson;

    public ImageConverterWebpAPIImpl(Context context) throws StickerFatalErrorException {
        super(BuildConfig.API_ENDPOINT, context);
        gson = new Gson();
    }

    public ResponseAPIConvertedWebp convertImageToWebp(String imageInBase64) throws StickerException {
        Call call = this.post("/converter", gson.toJson(new ConvertImageToWebpRQ(imageInBase64)));
        try (Response response = call.execute()) {
            return gson.fromJson(response.body().string(), ResponseAPIConvertedWebp.class);
        } catch (SocketTimeoutException ex) {
            throw new StickerWebCommunicationException(ex, StickerWebCommunicationExceptionEnum.TIMEOUT, "/converter");
        }  catch (IOException ex) {
            handleIOException(ex);
            return null;
        }
    }

    @Override
    public void warm() throws StickerException {
        Call call = this.post("/converter", "{\"warmup\": true}");
        try (Response response = call.execute()) {
            if (response.code() != 200) {
                throw new StickerWebCommunicationException(null, StickerWebCommunicationExceptionEnum.POST, "warm up error");
            }
        } catch (SocketTimeoutException ex) {
            throw new StickerWebCommunicationException(ex, StickerWebCommunicationExceptionEnum.TIMEOUT, "/converter");
        }  catch (IOException ex) {
            handleIOException(ex);
        }
    }

    private void handleIOException(IOException ex) throws StickerWebCommunicationException {
        if (this.isNetworkAvailable()) {
            throw new StickerWebCommunicationException(ex, StickerWebCommunicationExceptionEnum.POST, "Error converting image to webp via web service");
        } else {
            throw new StickerWebCommunicationException(null, StickerWebCommunicationExceptionEnum.NO_INTERNET_ACCESS, null);
        }
    }
}
