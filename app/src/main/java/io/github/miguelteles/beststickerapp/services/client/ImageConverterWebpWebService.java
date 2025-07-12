package io.github.miguelteles.beststickerapp.services.client;

import io.github.miguelteles.beststickerapp.domain.dto.ResponseConvertedWebpDTO;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ImageConverterWebpWebService {

    @POST("/image-converter/converter")
    public Call<ResponseConvertedWebpDTO> convertImage(@Body String imageInBase64);

}
