package io.github.miguelteles.beststickerapp.services.client;

import com.google.gson.Gson;

import java.io.IOException;

import io.github.miguelteles.beststickerapp.domain.pojo.ResponseAPIConvertedWebpDTO;
import io.github.miguelteles.beststickerapp.exception.StickerHttpClientException;
import io.github.miguelteles.beststickerapp.exception.enums.StickerHttpClientExceptionEnum;
import okhttp3.Call;
import okhttp3.Response;

public class ImageConverterWebpAPI extends HttpClient {

    private final Gson gson;

    public ImageConverterWebpAPI() {
        super("image-converter");
        gson = new Gson();
    }

    public ResponseAPIConvertedWebpDTO convertImageToWebp(String imageInBase64) throws StickerHttpClientException {
        Call call = this.post("/converter", imageInBase64);
        try (Response response = call.execute()){
            return gson.fromJson(response.body().string(), ResponseAPIConvertedWebpDTO.class);
        } catch (IOException ex) {
            throw new StickerHttpClientException(ex, StickerHttpClientExceptionEnum.POST, "Error converting image to webp via web service");
        }
    }
}
