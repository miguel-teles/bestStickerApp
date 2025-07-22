package io.github.miguelteles.beststickerapp.services.client;

import com.google.gson.Gson;

import java.io.IOException;

import io.github.miguelteles.beststickerapp.BuildConfig;
import io.github.miguelteles.beststickerapp.domain.pojo.ResponseAPIConvertedWebpDTO;
import io.github.miguelteles.beststickerapp.exception.StickerHttpClientException;
import io.github.miguelteles.beststickerapp.exception.enums.StickerHttpClientExceptionEnum;
import io.github.miguelteles.beststickerapp.services.client.interfaces.ImageConverterWebpAPI;
import okhttp3.Call;
import okhttp3.Response;

public class ImageConverterWebpImpl extends HttpClient implements ImageConverterWebpAPI {

    private final Gson gson;

    public ImageConverterWebpImpl() {
        super(BuildConfig.API_PRODUCTION_URL);
        gson = new Gson();
    }

    public ResponseAPIConvertedWebpDTO convertImageToWebp(String imageInBase64) throws StickerHttpClientException {

        Call call = this.post("/converter", gson.toJson(new ConvertImageToWebpRQ(imageInBase64)));
        try (Response response = call.execute()) {
            return gson.fromJson(response.body().string(), ResponseAPIConvertedWebpDTO.class);
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
