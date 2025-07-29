package io.github.miguelteles.beststickerapp.services.client;

import com.google.gson.Gson;

import java.io.IOException;

import io.github.miguelteles.beststickerapp.BuildConfig;
import io.github.miguelteles.beststickerapp.domain.pojo.ResponseAPIConvertedWebp;
import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.exception.StickerFatalErrorException;
import io.github.miguelteles.beststickerapp.exception.StickerHttpClientException;
import io.github.miguelteles.beststickerapp.exception.enums.StickerHttpClientExceptionEnum;
import io.github.miguelteles.beststickerapp.services.client.interfaces.ImageConverterWebpAPI;
import okhttp3.Call;
import okhttp3.Response;

public class ImageConverterWebpAPIImpl extends HttpClient implements ImageConverterWebpAPI {

    private final Gson gson;

    public ImageConverterWebpAPIImpl() throws StickerFatalErrorException {
        super(BuildConfig.API_ENDPOINT);
        gson = new Gson();
    }

    public ResponseAPIConvertedWebp convertImageToWebp(String imageInBase64) throws StickerException {
        Call call = this.post("/converter", gson.toJson(new ConvertImageToWebpRQ(imageInBase64)));
        try (Response response = call.execute()) {
            return gson.fromJson(response.body().string(), ResponseAPIConvertedWebp.class);
        } catch (IOException ex) {
            handleIOException(ex);
            return null;
        }
    }

    private void handleIOException(IOException ex) throws StickerHttpClientException {
        if (this.isNetworkAvailable()) {
            throw new StickerHttpClientException(ex, StickerHttpClientExceptionEnum.POST, "Error converting image to webp via web service");
        } else {
            throw new StickerHttpClientException(null, StickerHttpClientExceptionEnum.NO_INTERNET_ACCESS, null);
        }
    }

    private record ConvertImageToWebpRQ(String originalImageBase64) {
    }
}
