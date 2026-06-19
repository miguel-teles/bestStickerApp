package io.github.miguelteles.beststickerapp.services.mediaconvertion;

import android.content.ContentResolver;
import android.net.Uri;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import io.github.miguelteles.beststickerapp.domain.entity.Sticker;
import io.github.miguelteles.beststickerapp.domain.pojo.ResponseAPIDownloadSourceVideoConverter;
import io.github.miguelteles.beststickerapp.domain.pojo.ResponseAPIUploadDestinationVideoConverter;
import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.exception.StickerFolderException;
import io.github.miguelteles.beststickerapp.exception.enums.StickerExceptionEnum;
import io.github.miguelteles.beststickerapp.exception.enums.StickerFolderExceptionEnum;
import io.github.miguelteles.beststickerapp.services.FileResourceManagement;
import io.github.miguelteles.beststickerapp.services.client.VideoConverterWebpAPIImpl;
import io.github.miguelteles.beststickerapp.services.client.interfaces.VideoConverterWebpAPI;
import io.github.miguelteles.beststickerapp.services.interfaces.ResourcesManagement;
import io.github.miguelteles.beststickerapp.utils.Utils;

public class StickerVideoConvertionService extends StickerMediaConvertionService {

    private static StickerVideoConvertionService instance;
    private final VideoConverterWebpAPI videoConverterWebpAPI;

    private StickerVideoConvertionService() throws StickerException {
        super(FileResourceManagement.getInstance(), Utils.getApplicationContext().getContentResolver());
        videoConverterWebpAPI = new VideoConverterWebpAPIImpl(Utils.getApplicationContext());
    }

    public StickerVideoConvertionService(ResourcesManagement resourcesManagement,
                                         VideoConverterWebpAPI videoConverterWebpAPI,
                                         ContentResolver contentResolver) {
        super(resourcesManagement, contentResolver);
        this.videoConverterWebpAPI = videoConverterWebpAPI;
    }

    public ResourcesManagement.Media generateConvertedMedia(@NotNull Uri stickerPackFolder,
                                                            @NotNull Uri sourceImage,
                                                            @NotNull String destinationImageFileName) throws StickerException {
        return this.generateConvertedMedia(stickerPackFolder,
                sourceImage,
                destinationImageFileName,
                Sticker.STICKER_IMAGE_SIZE,
                false);
    }

    public ResourcesManagement.Media generateConvertedMedia(@NotNull Uri stickerPackFolder,
                                                            @NotNull Uri sourceImage,
                                                            @NotNull String destinationImageFileName,
                                                            @NotNull Integer imageWidthAndHeight,
                                                            boolean keepOriginalCopy) throws StickerException {
        Uri copiedVideo = null;
        try {
            copiedVideo = generateMediaCopy(sourceImage, resourcesManagement.getCacheFolder(), buildResizedImageFileName(sourceImage, destinationImageFileName));

            URL convertedVideoToWebp = convertVideoToWebp(copiedVideo);
            return ResourcesManagement.Media.builder()
                    .linkToDownloadMedia(convertedVideoToWebp)
                    .build();
        } catch (StickerException ste) {
            throw ste;
        } catch (Exception ex) {
            throw new StickerFolderException(ex, StickerFolderExceptionEnum.COPY, "Erro ao copiar foto do pacote para a pasta do pacote " + stickerPackFolder.getLastPathSegment());
        } finally {
            if (copiedVideo != null) {
                resourcesManagement.deleteFile(copiedVideo);
            }
        }
    }

    private URL convertVideoToWebp(Uri copiedVideo) throws StickerException {
        String filename = this.resourcesManagement.getFileFromURI(copiedVideo).getName();

        ResponseAPIUploadDestinationVideoConverter uploadDestination = this.videoConverterWebpAPI.createUploadDestination(filename);
        if (uploadDestination.getSignedUrl() == null) {
            throw new StickerFolderException(null, StickerFolderExceptionEnum.CONVERT_FILE, uploadDestination.getMessage());
        }

        byte[] contentAsBytes = this.resourcesManagement.getContentAsBytes(copiedVideo);
        this.videoConverterWebpAPI.uploadVideo(uploadDestination.getSignedUrl(), contentAsBytes);

        ResponseAPIDownloadSourceVideoConverter downloadSource = this.videoConverterWebpAPI.createDownloadSource(uploadDestination.getConvertedFileName());

        return createURL(downloadSource);
    }

    @NonNull
    private URL createURL(ResponseAPIDownloadSourceVideoConverter downloadSource) throws StickerException {
        try {
            return new URL(downloadSource.getSignedUrl());
        } catch (MalformedURLException e) {
            throw new StickerException(e, StickerExceptionEnum.IDL, "URL de download do vídeo convertido é inválido");
        }

    }

    public static StickerVideoConvertionService getInstance() throws StickerException {
        if (instance == null) {
            instance = new StickerVideoConvertionService();
        }
        return instance;
    }
}
