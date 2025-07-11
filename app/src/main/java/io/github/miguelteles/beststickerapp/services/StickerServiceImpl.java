package io.github.miguelteles.beststickerapp.services;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.exception.StickerFolderException;
import io.github.miguelteles.beststickerapp.exception.enums.StickerExceptionEnum;
import io.github.miguelteles.beststickerapp.domain.entity.Sticker;
import io.github.miguelteles.beststickerapp.domain.entity.StickerPack;
import io.github.miguelteles.beststickerapp.exception.enums.StickerFolderExceptionEnum;
import io.github.miguelteles.beststickerapp.repository.MyDatabase;
import io.github.miguelteles.beststickerapp.repository.StickerRepository;
import io.github.miguelteles.beststickerapp.repository.contentProvider.StickerUriProvider;
import io.github.miguelteles.beststickerapp.services.interfaces.FoldersManagementService;
import io.github.miguelteles.beststickerapp.services.interfaces.StickerService;
import io.github.miguelteles.beststickerapp.utils.Utils;
import io.github.miguelteles.beststickerapp.validator.StickerPackValidator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class StickerServiceImpl implements StickerService {

    private static StickerService instance;
    private final FoldersManagementService foldersManagementService;
    private final StickerRepository stickerRepository;
    private final StickerUriProvider stickerUriProvider;
    private final ContentResolver contentResolver;
    private final StickerPackValidator stickerPackValidator;

    private StickerServiceImpl(Context context) throws StickerException {
        this.stickerRepository = new StickerRepository(MyDatabase.getInstance().getSqLiteDatabase());
        this.stickerPackValidator = StickerPackValidator.getInstance();
        foldersManagementService = FoldersManagementServiceImpl.getInstance();
        stickerUriProvider = StickerUriProvider.getInstance();
        contentResolver = context.getContentResolver();
    }

    public StickerServiceImpl(StickerRepository stickerRepository,
                              FoldersManagementService foldersManagementService,
                              StickerUriProvider stickerUriProvider,
                              ContentResolver contentResolver,
                              StickerPackValidator stickerPackValidator) {
        this.stickerRepository = stickerRepository;
        this.foldersManagementService = foldersManagementService;
        this.stickerUriProvider = stickerUriProvider;
        this.contentResolver = contentResolver;
        this.stickerPackValidator = stickerPackValidator;
    }

    public static StickerService getInstance() throws StickerException {
        if (instance == null) {
            instance = new StickerServiceImpl(Utils.getApplicationContext());
        }
        return instance;
    }

    @Override
    public Sticker createSticker(StickerPack stickerPack,
                                 Uri selectedStickerImage) throws StickerException {
        validateParametersCreateSticker(stickerPack, selectedStickerImage);

        File stickerPackFolder = foldersManagementService.getStickerPackFolderByFolderName(stickerPack.getFolderName());
        FoldersManagementServiceImpl.Image copiedImages = foldersManagementService.generateStickerImages(stickerPackFolder,
                selectedStickerImage,
                generateStickerImageName(),
                FoldersManagementServiceImpl.STICKER_IMAGE_SIZE,
                false);

        Sticker sticker = new Sticker(copiedImages.getResizedImageFile().getName(), stickerPack.getIdentifier(), copiedImages.getResidezImageFileInBytes());
        stickerPackValidator.validateSticker(stickerPack.getIdentifier(), sticker, stickerPack.isAnimatedStickerPack());
        stickerRepository.save(sticker);
        if (sticker.getIdentifier() != null) {
            contentResolver.insert(stickerUriProvider.getStickerInsertUri(), sticker.toContentValues());
            return sticker;
        } else {
            throw new StickerException(null, StickerExceptionEnum.CSP, "Erro ao salvar pacote no banco");
        }
    }

    private void validateParametersCreateSticker(StickerPack stickerPack, Uri selectedStickerImage) {
        if (stickerPack == null) {
            throw new IllegalArgumentException("StickerPack parameter is mandatory");
        } else {
            if (stickerPack.getIdentifier() == null || Utils.isNothing(stickerPack.getFolderName())) {
                throw new IllegalArgumentException("StickerPack is missing data");
            }
        }
        if (selectedStickerImage == null) {
            throw new IllegalArgumentException("SelectedStickerImage parameter is mandatory");
        }
    }

    @Override
    public void deleteSticker(Sticker sticker,
                              StickerPack stickerPack) throws StickerException {
        validateParametersDeleteSticker(sticker, stickerPack);

        File stickerPackFolder = foldersManagementService.getStickerPackFolderByFolderName(stickerPack.getFolderName());
        foldersManagementService.deleteFile(new File(stickerPackFolder, sticker.getStickerImageFile()));

        contentResolver.delete(stickerUriProvider.getStickerDeleteUri(), null);
    }

    @Override
    public void deleteStickersFromStickerPack(Integer packIdentifier) throws StickerException {
        this.stickerRepository.removeByPackIdentifier(packIdentifier);
    }

    private void validateParametersDeleteSticker(Sticker sticker, StickerPack stickerPack) {
        if (sticker == null) {
            throw new IllegalArgumentException("Sticker cannot be null");
        }
        if (stickerPack == null) {
            throw new IllegalArgumentException("Stickerpack cannot be null");
        }
    }

    @Override
    public List<Sticker> fetchAllStickerFromPackWithAssets(Integer packIdentifier) throws StickerException {
        validateParametersFetchAllStickerFromPack(packIdentifier);
        List<Sticker> stickers = stickerRepository.findByPackIdentifier(packIdentifier);

        for (Sticker sticker : stickers) {
            final byte[] bytes;
            try {
                bytes = fetchStickerAsset(packIdentifier,
                        sticker.getStickerImageFile());
                if (bytes.length <= 0) {
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

    @Override
    public List<Sticker> fetchAllStickerFromPackWithoutAssets(Integer packIdentifier) throws StickerException {
        return stickerRepository.findByPackIdentifier(packIdentifier);
    }

    private void validateParametersFetchAllStickerFromPack(Integer packIdentifier) {
        if (packIdentifier == null) {
            throw new IllegalArgumentException("Sticker pack identifier cannot be null");
        }
    }

    /**
     * Busca um asset da pasta (imagem da figurinha ou da capa do sticker pack)
     **/
    @Override
    public byte[] fetchStickerAsset(@NonNull Integer packIdentifier, @NonNull String stickerImageFileName) throws StickerFolderException {
        //o contentResolver.openInputStream vai pro método openAssetFile do contentProvider
        try (final InputStream inputStream = contentResolver.openInputStream(stickerUriProvider.getStickerAssetUri(packIdentifier, stickerImageFileName))) {
            return foldersManagementService.readBytesFromInputStream(inputStream, stickerImageFileName);
        } catch (IOException ex) {
            throw new StickerFolderException(ex, StickerFolderExceptionEnum.GET_FILE, "Erro when fetching sticker asset");
        }
    }

    private String generateStickerImageName() {
        return "sticker" + Utils.formatData(new Date(), "yyyyMMddHHmmss");
    }
}
