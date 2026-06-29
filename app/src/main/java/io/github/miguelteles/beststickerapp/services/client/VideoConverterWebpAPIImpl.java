package io.github.miguelteles.beststickerapp.services.client;

import android.content.Context;

import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URL;

import io.github.miguelteles.beststickerapp.BuildConfig;
import io.github.miguelteles.beststickerapp.domain.pojo.GeneratePresignedURLUploadVideoRQ;
import io.github.miguelteles.beststickerapp.domain.pojo.ResponseAPIDownloadSourceVideoConverter;
import io.github.miguelteles.beststickerapp.domain.pojo.ResponseAPIUploadDestinationVideoConverter;
import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.exception.StickerFatalErrorException;
import io.github.miguelteles.beststickerapp.exception.StickerWebCommunicationException;
import io.github.miguelteles.beststickerapp.exception.enums.StickerWebCommunicationExceptionEnum;
import io.github.miguelteles.beststickerapp.services.client.interfaces.VideoConverterWebpAPI;
import io.github.miguelteles.beststickerapp.utils.Utils;
import okhttp3.Call;
import okhttp3.Response;

public class VideoConverterWebpAPIImpl extends HttpClient implements VideoConverterWebpAPI {

    private static VideoConverterWebpAPIImpl instance;
    private final Gson gson;
    private final String PRESIGNED_GENERATION_URL_UPLOAD_FILE = "/video-converter-url";

    public VideoConverterWebpAPIImpl(Context context) throws StickerFatalErrorException {
        super(BuildConfig.API_ENDPOINT, context);
        gson = new Gson();
    }

    public static VideoConverterWebpAPI getInstance() throws StickerFatalErrorException {
        if (instance == null) {
            instance = new VideoConverterWebpAPIImpl(Utils.getApplicationContext());
        }
        return instance;
    }

    @Override
    public ResponseAPIUploadDestinationVideoConverter createUploadDestination(String filename) throws StickerException {
        Call call = this.post(PRESIGNED_GENERATION_URL_UPLOAD_FILE, gson.toJson(new GeneratePresignedURLUploadVideoRQ(filename, GeneratePresignedURLUploadVideoRQ.OperationType.UPLOAD)));
        try (Response response = call.execute()) {
            return gson.fromJson(response.body().string(), ResponseAPIUploadDestinationVideoConverter.class);
        } catch (SocketTimeoutException ex) {
            throw new StickerWebCommunicationException(ex, StickerWebCommunicationExceptionEnum.TIMEOUT, PRESIGNED_GENERATION_URL_UPLOAD_FILE);
        }  catch (IOException ex) {
            handleIOException(ex);
            return null;
        }
    }

    @Override
    public ResponseAPIDownloadSourceVideoConverter createDownloadSource(String filename) throws StickerException {
        Call call = this.post(PRESIGNED_GENERATION_URL_UPLOAD_FILE, gson.toJson(new GeneratePresignedURLUploadVideoRQ(filename, GeneratePresignedURLUploadVideoRQ.OperationType.DOWNLOAD)));
        try (Response response = call.execute()) {
            return gson.fromJson(response.body().string(), ResponseAPIDownloadSourceVideoConverter.class);
        } catch (SocketTimeoutException ex) {
            throw new StickerWebCommunicationException(ex, StickerWebCommunicationExceptionEnum.TIMEOUT, PRESIGNED_GENERATION_URL_UPLOAD_FILE);
        }  catch (IOException ex) {
            handleIOException(ex);
            return null;
        }
    }

    @Override
    public Object uploadVideo(String presignedUrl, File video) throws StickerException {
        Call call = this.put(presignedUrl, video);
        try (Response response = call.execute()) {
            return response.body().string();
        } catch (SocketTimeoutException ex) {
            throw new StickerWebCommunicationException(ex, StickerWebCommunicationExceptionEnum.TIMEOUT, "presigned url");
        }  catch (IOException ex) {
            handleIOException(ex);
            return null;
        }
    }

    @Override
    public void warm() throws StickerException {
        Call call = this.post(PRESIGNED_GENERATION_URL_UPLOAD_FILE, "{\"warmup\": true}");
        try (Response response = call.execute()) {
            if (response.code() != 200) {
                throw new StickerWebCommunicationException(null, StickerWebCommunicationExceptionEnum.POST, "warm up error");
            }
        } catch (SocketTimeoutException ex) {
            throw new StickerWebCommunicationException(ex, StickerWebCommunicationExceptionEnum.TIMEOUT, PRESIGNED_GENERATION_URL_UPLOAD_FILE);
        }  catch (IOException ex) {
            handleIOException(ex);
        }
    }

    private void handleIOException(IOException ex) throws StickerWebCommunicationException {
        if (this.isNetworkAvailable()) {
            throw new StickerWebCommunicationException(ex, StickerWebCommunicationExceptionEnum.POST, "Error converting video to webp via web service");
        } else {
            throw new StickerWebCommunicationException(null, StickerWebCommunicationExceptionEnum.NO_INTERNET_ACCESS, null);
        }
    }
}
