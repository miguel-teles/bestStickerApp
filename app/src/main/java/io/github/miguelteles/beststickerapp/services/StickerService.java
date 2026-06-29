package io.github.miguelteles.beststickerapp.services;

import static io.github.miguelteles.beststickerapp.exception.enums.StickerFolderExceptionEnum.FILE_NOT_FOUND;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import io.github.miguelteles.beststickerapp.domain.entity.Sticker;
import io.github.miguelteles.beststickerapp.domain.entity.StickerPack;
import io.github.miguelteles.beststickerapp.domain.pojo.VisualMediaType;
import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.exception.StickerFolderException;
import io.github.miguelteles.beststickerapp.exception.enums.StickerExceptionEnum;
import io.github.miguelteles.beststickerapp.exception.enums.StickerFolderExceptionEnum;
import io.github.miguelteles.beststickerapp.repository.MyDatabase;
import io.github.miguelteles.beststickerapp.repository.StickerRepository;
import io.github.miguelteles.beststickerapp.services.interfaces.operationcallback.OnProgressUpdate;
import io.github.miguelteles.beststickerapp.services.interfaces.ResourcesManagement;
import io.github.miguelteles.beststickerapp.services.mediaconvertion.StickerImageConvertionService;
import io.github.miguelteles.beststickerapp.services.mediaconvertion.StickerVideoConvertionService;
import io.github.miguelteles.beststickerapp.utils.Utils;
import io.github.miguelteles.beststickerapp.validator.StickerPackValidator;

public class StickerService {

    private static StickerService instance;
    private final ResourcesManagement resourcesManagement;
    private final StickerImageConvertionService stickerImageConvertionService;
    private final StickerVideoConvertionService stickerVideoConvertionService;
    private final StickerRepository stickerRepository;
    private final ContentResolver contentResolver;
    private final StickerPackValidator stickerPackValidator;

    private StickerService(Context context) throws StickerException {
        this.stickerRepository = new StickerRepository(MyDatabase.getInstance().getSqLiteDatabase());
        this.stickerPackValidator = StickerPackValidator.getInstance();
        this.resourcesManagement = FileResourceManagement.getInstance();
        this.contentResolver = context.getContentResolver();
        this.stickerImageConvertionService = StickerImageConvertionService.getInstance();
        this.stickerVideoConvertionService = StickerVideoConvertionService.getInstance();
    }

    public StickerService(StickerRepository stickerRepository,
                          ResourcesManagement resourcesManagement,
                          ContentResolver contentResolver,
                          StickerPackValidator stickerPackValidator,
                          StickerImageConvertionService stickerImageConvertionService,
                          StickerVideoConvertionService stickerVideoConvertionService) {
        this.stickerRepository = stickerRepository;
        this.resourcesManagement = resourcesManagement;
        this.contentResolver = contentResolver;
        this.stickerPackValidator = stickerPackValidator;
        this.stickerImageConvertionService = stickerImageConvertionService;
        this.stickerVideoConvertionService = stickerVideoConvertionService;
    }

    public static StickerService getInstance() throws StickerException {
        if (instance == null) {
            instance = new StickerService(Utils.getApplicationContext());
        }
        return instance;
    }

    public Sticker createSticker(StickerPack stickerPack,
                                 Uri selectedStickerImage,
                                 OnProgressUpdate callbackClass) throws StickerException {
        callbackClass.onProgressUpdate(30);
        SavedMedia savedMedia = null;
        try {
            validateSelectedMediaAndPackType(stickerPack, selectedStickerImage);
            Uri stickerPackFolder = resourcesManagement.getOrCreateStickerPackDirectory(stickerPack.getFolderName());
            String stickerImageFile = generateStickerImageName();
            savedMedia = convertAndSaveMedia(stickerPack, selectedStickerImage, stickerPackFolder, stickerImageFile, callbackClass);

            callbackClass.onProgressUpdate();
            Sticker sticker = buildSticker(stickerPack, savedMedia);
            stickerPackValidator.validateSticker(stickerPack.getIdentifier(), sticker, stickerPack.isAnimatedStickerPack());
            stickerRepository.save(sticker);

            callbackClass.onProgressUpdate();
            return sticker;
        } catch (StickerException ex) {
            throw ex;
        } catch (Exception ex) {
            deleteStickerImages(savedMedia);
            throw new StickerException(ex, StickerExceptionEnum.CSP, null);
        }
    }

    private void validateSelectedMediaAndPackType(StickerPack stickerPack, Uri selectedStickerImage) throws StickerException {
        VisualMediaType typeOfVisualMedia = resourcesManagement.getTypeOfVisualMedia(selectedStickerImage);
        if (stickerPack.isStandardStickerPack() && (typeOfVisualMedia.isAnimated())) {
            throw new StickerException(null, StickerExceptionEnum.WTSP, "Tipo do pacote é padrão, porém figurinha é animada");
        } else if (stickerPack.isAnimatedStickerPack() && typeOfVisualMedia.isImage()) {
            throw new StickerException(null, StickerExceptionEnum.WTSP, "Tipo do pacote é animado, porém figurinha é estática");
        }
    }

    @NonNull
    private static Sticker buildSticker(StickerPack stickerPack, SavedMedia savedMedia) {
        return new Sticker(savedMedia.savedImage().getLastPathSegment(),
                stickerPack.getIdentifier(),
                savedMedia.stickerImageInBytes());
    }

    @NonNull
    private SavedMedia convertAndSaveMedia(StickerPack stickerPack,
                                           Uri selectedStickerImage,
                                           Uri stickerPackFolder,
                                           String stickerImageFile,
                                           OnProgressUpdate callbackClass) throws StickerException {
        if (stickerPack.isStandardStickerPack()) {
            return convertToStaticWebpAndSave(selectedStickerImage, stickerPackFolder, stickerImageFile, callbackClass);
        } else {
            return convertToAnimatedWebpAndSave(selectedStickerImage, stickerPackFolder, stickerImageFile, callbackClass);
        }
    }

    @NonNull
    private SavedMedia convertToAnimatedWebpAndSave(Uri selectedStickerImage,
                                                    Uri stickerPackFolder,
                                                    String stickerImageFile,
                                                    OnProgressUpdate callbackClass) throws StickerException {
        ResourcesManagement.Media copiedImages = stickerVideoConvertionService.generateConvertedMedia(stickerPackFolder,
                selectedStickerImage,
                stickerImageFile,
                callbackClass);
        callbackClass.onProgressUpdate();
        Uri savedImage = saveConvertedVideoToDevice(stickerImageFile,
                stickerPackFolder,
                copiedImages.getLinkToDownloadMedia(),
                callbackClass);
        callbackClass.onProgressUpdate();
        return new SavedMedia(resourcesManagement.getContentAsBytes(savedImage),
                savedImage);
    }

    @NonNull
    private SavedMedia convertToStaticWebpAndSave(Uri selectedStickerImage,
                                                  Uri stickerPackFolder,
                                                  String stickerImageFile,
                                                  OnProgressUpdate callbackClass) throws StickerException {
        ResourcesManagement.Media copiedImages = stickerImageConvertionService.generateConvertedMedia(stickerPackFolder,
                selectedStickerImage,
                stickerImageFile,
                Sticker.STICKER_IMAGE_SIZE,
                false,
                callbackClass);
        callbackClass.onProgressUpdate();
        Uri savedImage = saveConvertedImageToDevice(stickerPackFolder, copiedImages.getConvertedMedia());
        callbackClass.onProgressUpdate();
        return new SavedMedia(copiedImages.getConvertedMedia(),
                savedImage);
    }

    private Uri saveConvertedVideoToDevice(String stickerImageFile,
                                           Uri stickerPackFolder,
                                           URL linkToDownloadMedia,
                                           OnProgressUpdate onProgressUpdate) throws StickerException {
        Uri stickerUri = resourcesManagement.getOrCreateFile(stickerPackFolder, stickerImageFile);

        downloadVideoWithRetries(linkToDownloadMedia, stickerUri, onProgressUpdate);

        return stickerUri;
    }

    private void downloadVideoWithRetries(URL linkToDownloadMedia, Uri stickerUri, OnProgressUpdate onProgressUpdate) throws StickerException {
        int maxRetries = 10;
        for (int i = 1; i <= maxRetries; i++) {
            try {
                this.resourcesManagement.downloadFile(
                        linkToDownloadMedia,
                        resourcesManagement.getFileFromURI(stickerUri));
                break;
            } catch (StickerFolderException ex) {
                if (!FILE_NOT_FOUND.toString().equals(ex.getStickerExceptionEnumMessage())) {
                    throw ex;
                }
                if (FILE_NOT_FOUND.toString().equals(ex.getStickerExceptionEnumMessage()) && i == maxRetries) {
                    throw new StickerException(null, StickerExceptionEnum.CTO, null);
                }
                aguardaConversao();
            }
            onProgressUpdate.onProgressUpdate(1);
        }
        ;
    }

    private static void aguardaConversao() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private Uri saveConvertedImageToDevice(@NonNull Uri stickerPackFolder,
                                           byte[] convertImageToWebp) throws StickerFolderException {
        return this.resourcesManagement.saveFileToDevice(
                stickerPackFolder,
                createNameConvertedImageFile(),
                convertImageToWebp
        );
    }

    private String createNameConvertedImageFile() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private void deleteStickerImages(SavedMedia savedMedia) {
        try {
            if (savedMedia != null) {
                resourcesManagement.deleteFile(savedMedia.savedImage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteSticker(Sticker sticker,
                              StickerPack stickerPack) throws StickerException {
        validateParametersDeleteSticker(sticker, stickerPack);

        Uri stickerPackFolder = resourcesManagement.getOrCreateStickerPackDirectory(stickerPack.getFolderName());
        resourcesManagement.deleteFile(Uri.withAppendedPath(stickerPackFolder, sticker.getStickerImageFile()));
        stickerRepository.remove(sticker);
    }

    private void validateParametersDeleteSticker(Sticker sticker, StickerPack stickerPack) {
        if (sticker == null) {
            throw new IllegalArgumentException("Sticker cannot be null");
        }
        if (stickerPack == null) {
            throw new IllegalArgumentException("Stickerpack cannot be null");
        }
    }

    public List<Sticker> fetchAllStickerFromPackWithAssets(UUID packIdentifier, String folderName) throws StickerException {
        validateParametersFetchAllStickerFromPack(packIdentifier);
        List<Sticker> stickers = fetchAllStickerFromPackWithoutAssets(packIdentifier);

        for (Sticker sticker : stickers) {
            final byte[] bytes;
            try {
                bytes = fetchStickerAsset(folderName,
                        sticker.getStickerImageFile());
                if (bytes.length == 0) {
                    throw new IllegalStateException("Asset file is empty, pack identifier: " + packIdentifier + ", sticker: " + sticker.getStickerImageFile());
                }
                sticker.setSize(bytes.length);
                sticker.setStickerImageFileInBytes(bytes);
            } catch (IllegalArgumentException e) {
                throw new StickerException(e, StickerExceptionEnum.FS, "Erro ao buscar figurinhas do pacote " + packIdentifier);
            }
        }

        return stickers;
    }

    public List<Sticker> fetchAllStickerFromPackWithoutAssets(UUID packIdentifier) throws StickerException {
        return stickerRepository.findByPackIdentifier(packIdentifier);
    }

    private void validateParametersFetchAllStickerFromPack(UUID packIdentifier) {
        if (packIdentifier == null) {
            throw new IllegalArgumentException("Sticker pack identifier cannot be null");
        }
    }

    /**
     * Busca um asset da pasta (imagem da figurinha ou da capa do sticker pack)
     **/
    public byte[] fetchStickerAsset(@NonNull String folderName, @NonNull String stickerImageFileName) throws StickerFolderException {
        //o contentResolver.openInputStream vai pro método openAssetFile do contentProvider
        try (final InputStream inputStream = contentResolver.openInputStream(resourcesManagement.getOrCreateFile(resourcesManagement.getOrCreateStickerPackDirectory(folderName), stickerImageFileName))) {
            return resourcesManagement.readBytesFromInputStream(inputStream);
        } catch (IOException ex) {
            throw new StickerFolderException(ex, StickerFolderExceptionEnum.GET_FILE, "Erro when fetching sticker asset");
        }
    }

    private String generateStickerImageName() {
        return "sticker" + Utils.formatData(new Date(), "yyyyMMddHHmmss");
    }

    private record SavedMedia(byte[] stickerImageInBytes, Uri savedImage) {
    }
}
