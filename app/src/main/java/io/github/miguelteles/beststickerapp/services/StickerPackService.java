package io.github.miguelteles.beststickerapp.services;

import android.content.ContentResolver;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
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
import io.github.miguelteles.beststickerapp.repository.StickerPackRepository;
import io.github.miguelteles.beststickerapp.services.interfaces.ResourcesManagement;
import io.github.miguelteles.beststickerapp.services.interfaces.operationcallback.OnProgressUpdate;
import io.github.miguelteles.beststickerapp.services.mediaconvertion.StickerImageConvertionService;
import io.github.miguelteles.beststickerapp.utils.Utils;
import io.github.miguelteles.beststickerapp.validator.StickerPackValidator;

public class StickerPackService {

    private static StickerPackService instance;
    private final ResourcesManagement resourceManagement;
    private final StickerPackRepository stickerPackRepository;
    private final ContentResolver contentResolver;
    private final StickerService stickerService;
    private final StickerPackValidator stickerPackValidator;
    private final StickerImageConvertionService stickerImageConvertionService;

    private StickerPackService() throws StickerException {
        this.stickerPackRepository = new StickerPackRepository(MyDatabase.getInstance().getSqLiteDatabase());
        this.resourceManagement = FileResourceManagement.getInstance();
        this.contentResolver = Utils.getApplicationContext().getContentResolver();
        this.stickerPackValidator = StickerPackValidator.getInstance();
        this.stickerService = StickerService.getInstance();
        this.stickerImageConvertionService = StickerImageConvertionService.getInstance();
    }

    public StickerPackService(ResourcesManagement resourceManagement,
                              StickerPackRepository stickerPackRepository,
                              ContentResolver contentResolver,
                              StickerService stickerService,
                              StickerPackValidator stickerPackValidator,
                              StickerImageConvertionService stickerImageConvertionService) {
        this.resourceManagement = resourceManagement;
        this.stickerPackRepository = stickerPackRepository;
        this.contentResolver = contentResolver;
        this.stickerService = stickerService;
        this.stickerPackValidator = stickerPackValidator;
        this.stickerImageConvertionService = stickerImageConvertionService;
    }

    public static StickerPackService getInstance() throws StickerException {
        if (instance == null) {
            instance = new StickerPackService();
        }
        return instance;
    }

    public StickerPack createStickerPack(@NonNull String authorName,
                                         @NonNull String packName,
                                         @NonNull Uri imageUri,
                                         boolean isAnimatedStickerPack,
                                         @NonNull OnProgressUpdate callbackClass) throws StickerException {
        Uri stickerPackFolder = null;
        try {
            callbackClass.onProgressUpdate(10);

            stickerPackFolder = resourceManagement.getOrCreateStickerPackDirectory(createStickerPackFolderName(packName));
            String packImageFilename = generateStickerPackImageName();
            ResourcesManagement.Media copiedImages = stickerImageConvertionService.generateConvertedMedia(stickerPackFolder,
                    imageUri,
                    packImageFilename,
                    StickerPack.TRAY_IMAGE_SIZE,
                    true,
                    OnProgressUpdate.EMPTY);
            callbackClass.onProgressUpdate(50);
            Uri savedFile = saveConvertedImageToDevice(stickerPackFolder, packImageFilename, copiedImages.getConvertedMedia());

            StickerPack stickerPack = buildStickerPack(authorName, packName, copiedImages, savedFile, stickerPackFolder, isAnimatedStickerPack);

            stickerPackValidator.verifyCreatedStickerPackValidity(stickerPack);
            stickerPackRepository.save(stickerPack);

            callbackClass.onProgressUpdate(70);
            return stickerPack;
        } catch (StickerException ex) {
            deletePackFolderOnException(stickerPackFolder);
            throw ex;
        } catch (Exception ex) {
            deletePackFolderOnException(stickerPackFolder);
            throw new StickerException(ex, StickerExceptionEnum.CSP, null);
        } finally {
            callbackClass.onProgressUpdate(90);
        }
    }

    @NonNull
    private static StickerPack buildStickerPack(@NonNull String authorName,
                                                @NonNull String packName,
                                                ResourcesManagement.Media copiedImages,
                                                Uri savedFile,
                                                Uri stickerPackFolder,
                                                boolean isAnimatedStickerPack) {
        return new StickerPack(packName,
                authorName,
                copiedImages.getOriginalImageFile().getLastPathSegment(),
                savedFile.getLastPathSegment(),
                stickerPackFolder.getLastPathSegment(),
                1,
                isAnimatedStickerPack,
                copiedImages.getConvertedMedia());
    }

    @NonNull
    private static String createStickerPackFolderName(@NonNull String packNameInput) {
        return packNameInput + Utils.formatData(new Date(), "yyyy.MM.dd.HH.mm.ss");
    }

    @NonNull
    private String generateStickerPackImageName() {
        return "packImg" + Utils.formatData(new Date(), "yyyyMMddHHmmss");
    }

    private void deletePackFolderOnException(Uri stickerPackFolder) {
        try {
            resourceManagement.deleteFile(stickerPackFolder);
        } catch (Exception e) {
        }
    }

    private Uri saveConvertedImageToDevice(@NonNull Uri stickerPackFolder,
                                           String filename,
                                           byte[] convertImageToWebp) throws StickerFolderException {
        return this.resourceManagement.saveFileToDevice(stickerPackFolder,
                filename,
                convertImageToWebp);
    }

    public StickerPack updateStickerPack(StickerPack stickerPack,
                                         String editedAuthorName,
                                         String editedPackName,
                                         OnProgressUpdate callback) throws StickerException {
        callback.onProgressUpdate(20);
        validateParametersUpdateStickerPack(stickerPack, editedPackName);

        stickerPack.setName(editedPackName);
        stickerPack.setPublisher(editedAuthorName);

        stickerPackRepository.update(stickerPack);
        callback.onProgressUpdate(70);
        callback.onProgressUpdate(90);
        return stickerPack;
    }

    public void updateStickerPackVersion(StickerPack stickerPack) throws StickerException {
        stickerPackRepository.updateStickerPackVersion(stickerPack.getIdentifier());
    }

    private void validateParametersUpdateStickerPack(StickerPack stickerPack,
                                                     String nomePacote) {
        if (stickerPack == null) {
            throw new IllegalArgumentException("Sticker pack cannot be null");
        }
        if (Utils.isNothing(nomePacote)) {
            throw new IllegalArgumentException("Nome pacote cannot be null");
        }
    }

    public void deleteStickerPack(StickerPack stickerPack,
                                  OnProgressUpdate callbackClass) throws StickerException {
        callbackClass.onProgressUpdate(20);
        stickerPackRepository.remove(stickerPack);

        callbackClass.onProgressUpdate(60);
        resourceManagement.deleteFile(resourceManagement.getOrCreateStickerPackDirectory(stickerPack.getFolderName()));
        callbackClass.onProgressUpdate(90);
    }

    public StickerPack fetchStickerPackAssets(StickerPack stickerPack) throws StickerException {
        StickerPack pack = stickerPackRepository.findById(stickerPack.getIdentifier());
        loadStickerPackAssets(pack);
        return pack;
    }

    public List<StickerPack> fetchAllStickerPacksWithAssets() throws StickerException {
        List<StickerPack> packs = stickerPackRepository.findAll();
        for (StickerPack pack : packs) {
            loadStickerPackAssets(pack);
        }
        return packs;
    }

    private void loadStickerPackAssets(StickerPack pack) throws StickerException {
        if (pack != null) {
            pack.setStickers(stickerService.fetchAllStickerFromPackWithAssets(pack.getIdentifier(), pack.getFolderName()));
            final byte[] bytes;
            try {
                bytes = fetchStickerPackAsset(pack.getFolderName(),
                        pack.getResizedTrayImageFile());
                if (bytes.length == 0) {
                    throw new IllegalStateException("Asset file is empty, pack identifier: " + pack.getIdentifier());
                }
                pack.setResizedTrayImageFileInBytes(bytes);
            } catch (IllegalArgumentException e) {
                throw new StickerException(e, StickerExceptionEnum.FS, "Erro ao buscar figurinhas do pacote " + pack.getIdentifier());
            }
        }
    }

    public List<StickerPack> fetchAllStickerPacksWithoutAssets() throws StickerException {
        List<StickerPack> packs = stickerPackRepository.findAll();
        for (StickerPack pack : packs) {
            pack.setStickers(stickerService.fetchAllStickerFromPackWithoutAssets(pack.getIdentifier()));
        }
        return packs;
    }

    public StickerPack fetchStickerPackWithoutAssets(UUID identifier) throws StickerException {
        StickerPack pack = stickerPackRepository.findById(identifier);
        pack.setStickers(stickerService.fetchAllStickerFromPackWithoutAssets(pack.getIdentifier()));
        return pack;
    }

    public byte[] fetchStickerPackAsset(@NonNull String folderName, @NonNull String stickerPackImageFileName) throws StickerFolderException {
        try (final InputStream inputStream = contentResolver.openInputStream(this.resourceManagement.getStickerRelatedFile(folderName, stickerPackImageFileName));
             final ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            if (inputStream == null) {
                throw new IOException("cannot read sticker pack asset folder: " + folderName + "; name: " + stickerPackImageFileName);
            }
            int read;
            byte[] data = new byte[16384];

            while ((read = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, read);
            }
            return buffer.toByteArray();
        } catch (IOException ex) {
            throw new StickerFolderException(ex, StickerFolderExceptionEnum.GET_FILE, "Erro when fetching sticker pack asset");
        }
    }
}
