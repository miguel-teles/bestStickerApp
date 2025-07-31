package io.github.miguelteles.beststickerapp.services;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import io.github.miguelteles.beststickerapp.domain.entity.Sticker;
import io.github.miguelteles.beststickerapp.domain.entity.StickerPack;
import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.exception.StickerFolderException;
import io.github.miguelteles.beststickerapp.exception.enums.StickerExceptionEnum;
import io.github.miguelteles.beststickerapp.exception.enums.StickerFolderExceptionEnum;
import io.github.miguelteles.beststickerapp.repository.MyDatabase;
import io.github.miguelteles.beststickerapp.repository.StickerRepository;
import io.github.miguelteles.beststickerapp.repository.contentProvider.StickerUriProvider;
import io.github.miguelteles.beststickerapp.services.interfaces.EntityOperationCallback;
import io.github.miguelteles.beststickerapp.services.interfaces.ResourcesManagement;
import io.github.miguelteles.beststickerapp.utils.Utils;
import io.github.miguelteles.beststickerapp.validator.MethodInputValidator;
import io.github.miguelteles.beststickerapp.validator.StickerPackValidator;

public class StickerService {

    private static StickerService instance;
    private final ResourcesManagement resourcesManagement;
    private final StickerImageConvertionService stickerImageConvertionService;
    private final StickerRepository stickerRepository;
    private final StickerUriProvider stickerUriProvider;
    private final ContentResolver contentResolver;
    private final StickerPackValidator stickerPackValidator;

    private StickerService(Context context) throws StickerException {
        this.stickerRepository = new StickerRepository(MyDatabase.getInstance().getSqLiteDatabase());
        this.stickerPackValidator = StickerPackValidator.getInstance();
        resourcesManagement = FileResourceManagement.getInstance();
        stickerUriProvider = StickerUriProvider.getInstance();
        contentResolver = context.getContentResolver();
        this.stickerImageConvertionService = StickerImageConvertionService.getInstance();
    }

    public StickerService(StickerRepository stickerRepository,
                          ResourcesManagement resourcesManagement,
                          StickerUriProvider stickerUriProvider,
                          ContentResolver contentResolver,
                          StickerPackValidator stickerPackValidator,
                          StickerImageConvertionService stickerImageConvertionService) {
        this.stickerRepository = stickerRepository;
        this.resourcesManagement = resourcesManagement;
        this.stickerUriProvider = stickerUriProvider;
        this.contentResolver = contentResolver;
        this.stickerPackValidator = stickerPackValidator;
        this.stickerImageConvertionService = stickerImageConvertionService;
    }

    public static StickerService getInstance() throws StickerException {
        if (instance == null) {
            instance = new StickerService(Utils.getApplicationContext());
        }
        return instance;
    }

    public Sticker createSticker(StickerPack stickerPack,
                                 Uri selectedStickerImage,
                                 EntityOperationCallback<Sticker> callbackClass) throws StickerException {
        validateParametersCreateSticker(stickerPack, selectedStickerImage, callbackClass);
        ResourcesManagement.Image copiedImages = null;
        try {
            callbackClass.onProgressUpdate(30);
            Uri stickerPackFolder = resourcesManagement.getOrCreateStickerPackDirectory(stickerPack.getFolderName());
            copiedImages = stickerImageConvertionService.generateStickerImages(stickerPackFolder,
                    selectedStickerImage,
                    generateStickerImageName(),
                    Sticker.STICKER_IMAGE_SIZE,
                    false);

            callbackClass.onProgressUpdate(50);
            Sticker sticker = new Sticker(copiedImages.resizedImageFile().getLastPathSegment(), stickerPack.getIdentifier(), copiedImages.residezImageFileInBytes());
            stickerPackValidator.validateSticker(stickerPack.getIdentifier(), sticker, stickerPack.isAnimatedStickerPack());
            stickerRepository.save(sticker);

            callbackClass.onProgressUpdate(70);
            return sticker;
        } catch (StickerException ex) {
            throw ex;
        } catch (Exception ex) {
            deleteStickerImages(copiedImages);
            throw new StickerException(ex, StickerExceptionEnum.CSP, null);
        }

    }

    private void deleteStickerImages(ResourcesManagement.Image copiedImages) {
        try {
            resourcesManagement.deleteFile(copiedImages.resizedImageFile());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void validateParametersCreateSticker(StickerPack stickerPack, Uri selectedStickerImage, EntityOperationCallback<Sticker> callbackClass) {
        MethodInputValidator.requireNotNull(stickerPack, "StickerPack");
        MethodInputValidator.requireNotNull(stickerPack.getIdentifier(), "StickerPack identifier");
        MethodInputValidator.requireNotEmpty(stickerPack.getFolderName(), "StickerPack folder");
        MethodInputValidator.requireNotNull(selectedStickerImage, "SelectedStickerImage");
        MethodInputValidator.requireNotNull(callbackClass, "CallbackClass");
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
        List<Sticker> stickers = stickerRepository.findByPackIdentifier(packIdentifier);

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
}
