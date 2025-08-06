package io.github.miguelteles.beststickerapp.services;

import android.content.ContentResolver;
import android.content.res.Resources;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.github.miguelteles.beststickerapp.R;
import io.github.miguelteles.beststickerapp.domain.entity.Sticker;
import io.github.miguelteles.beststickerapp.domain.entity.StickerPack;
import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.exception.StickerFolderException;
import io.github.miguelteles.beststickerapp.exception.enums.StickerExceptionEnum;
import io.github.miguelteles.beststickerapp.exception.enums.StickerFolderExceptionEnum;
import io.github.miguelteles.beststickerapp.repository.MyDatabase;
import io.github.miguelteles.beststickerapp.repository.StickerPackRepository;
import io.github.miguelteles.beststickerapp.services.interfaces.OperationCallback;
import io.github.miguelteles.beststickerapp.services.interfaces.ResourcesManagement;
import io.github.miguelteles.beststickerapp.utils.Utils;
import io.github.miguelteles.beststickerapp.validator.MethodInputValidator;
import io.github.miguelteles.beststickerapp.validator.StickerPackValidator;
import io.github.miguelteles.beststickerapp.view.interfaces.UiThreadPoster;
import io.github.miguelteles.beststickerapp.view.threadHandlers.AndroidUiThreadPoster;
import io.github.miguelteles.beststickerapp.view.threadHandlers.ImmediateUiThreadPoster;

public class StickerPackService {

    private static StickerPackService instance;
    private final ResourcesManagement resourceManagement;
    private final StickerPackRepository stickerPackRepository;
    private final ContentResolver contentResolver;
    private final StickerService stickerService;
    private final StickerPackValidator stickerPackValidator;
    private final StickerImageConvertionService stickerImageConvertionService;
    private final Resources resources;
    private final Executor executor;
    private final UiThreadPoster threadResultPoster;


    private StickerPackService() throws StickerException {
        this.stickerPackRepository = new StickerPackRepository(MyDatabase.getInstance().getSqLiteDatabase());
        this.resourceManagement = FileResourceManagement.getInstance();
        this.contentResolver = Utils.getApplicationContext().getContentResolver();
        this.stickerPackValidator = StickerPackValidator.getInstance();
        this.stickerService = StickerService.getInstance();
        this.resources = Utils.getApplicationContext().getResources();
        this.executor = Executors.newSingleThreadExecutor(); //roda criando uma nova  thread
        this.threadResultPoster = new AndroidUiThreadPoster();
        this.stickerImageConvertionService = StickerImageConvertionService.getInstance();
    }

    public StickerPackService(ResourcesManagement resourceManagement,
                              StickerPackRepository stickerPackRepository,
                              ContentResolver contentResolver,
                              StickerService stickerService,
                              StickerPackValidator stickerPackValidator,
                              StickerImageConvertionService stickerImageConvertionService,
                              Resources resources) {
        this.resourceManagement = resourceManagement;
        this.stickerPackRepository = stickerPackRepository;
        this.contentResolver = contentResolver;
        this.stickerService = stickerService;
        this.stickerPackValidator = stickerPackValidator;
        this.resources = resources;
        this.executor = Runnable::run; //roda sem criar uma nova thread
        this.threadResultPoster = new ImmediateUiThreadPoster();
        this.stickerImageConvertionService = stickerImageConvertionService;
    }

    public static StickerPackService getInstance() throws StickerException {
        if (instance == null) {
            instance = new StickerPackService();
        }
        return instance;
    }

    public void createStickerPack(@NonNull String authorNameInput,
                                  @NonNull String packNameInput,
                                  @NonNull Uri selectedImagemUri,
                                  @NonNull OperationCallback<StickerPack> callbackClass) {
        validateParametersCreateStickerPack(packNameInput, selectedImagemUri, callbackClass);
        final String authorName = determineAuthorName(authorNameInput);

        //esse cara aqui serve pra criar outra thread sem ser a principal pra processar
        executor.execute(() -> {
            StickerPack stickerPack = null;
            StickerException exception = null;

            Uri stickerPackFolder = null;
            try {
                callbackClass.onProgressUpdate(10);

                String stickerPackFolderName = packNameInput + Utils.formatData(new Date(), "yyyy.MM.dd.HH.mm.ss");
                stickerPackFolder = resourceManagement.getOrCreateStickerPackDirectory(stickerPackFolderName);
                ResourcesManagement.Image copiedImages = stickerImageConvertionService.generateStickerImages(stickerPackFolder,
                        selectedImagemUri,
                        generateStickerPackImageName(),
                        StickerPack.TRAY_IMAGE_SIZE,
                        true);
                callbackClass.onProgressUpdate(50);

                stickerPack = new StickerPack(null,
                        packNameInput,
                        authorName,
                        copiedImages.originalImageFile().getLastPathSegment(),
                        copiedImages.resizedImageFile().getLastPathSegment(),
                        stickerPackFolderName,
                        1,
                        false,
                        copiedImages.residezImageFileInBytes());
                stickerPackValidator.verifyCreatedStickerPackValidity(stickerPack);
                stickerPackRepository.save(stickerPack);
                callbackClass.onProgressUpdate(70);

            } catch (StickerException ex) {
                deletePackFolderOnException(stickerPackFolder);
                exception = ex;
            } catch (Exception ex) {
                deletePackFolderOnException(stickerPackFolder);
                exception = new StickerException(ex, StickerExceptionEnum.CSP, null);
            }
            callbackClass.onProgressUpdate(90);

            StickerException finalException = exception;
            StickerPack finalStickerPack = stickerPack;
            threadResultPoster.post(() -> {
                callbackClass.onCreationFinish(finalStickerPack, finalException);
            });
        });

    }

    @NonNull
    private String determineAuthorName(@NonNull String authorNameInput) {
        if (Utils.isNothing(authorNameInput)) {
            authorNameInput = resources.getString(R.string.defaultPublisher);
        }
        final String authorName = authorNameInput;
        return authorName;
    }

    private void validateParametersCreateStickerPack(String packName, Uri packImageUri, OperationCallback<StickerPack> callback) {
        MethodInputValidator.requireNotNull(packName, "Pack name");
        MethodInputValidator.requireNotEmpty(packName, "Pack name");

        MethodInputValidator.requireNotNull(packImageUri, "Pack requires");
        MethodInputValidator.requireNotNull(callback, "Callback");
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

    public void updateStickerPack(StickerPack stickerPack,
                                  String editedAuthorName,
                                  String editedPackName,
                                  OperationCallback<StickerPack> callback) {
        final String _editedAuthorName = determineAuthorName(editedAuthorName);
        executor.execute(() -> {
            StickerException exception = null;
            StickerPack updatedStickerPack = null;
            try {
                callback.onProgressUpdate(20);
                validateParametersUpdateStickerPack(stickerPack, editedPackName);

                stickerPack.setName(editedPackName);
                stickerPack.setPublisher(_editedAuthorName);

                updatedStickerPack = stickerPackRepository.update(stickerPack);
                callback.onProgressUpdate(70);
                callback.onProgressUpdate(90);
            } catch (StickerException ex) {
                exception = ex;
            }
            StickerException finalException = exception;
            StickerPack finalUpdatedStickerPack = updatedStickerPack;
            threadResultPoster.post(() -> {
                callback.onCreationFinish(finalUpdatedStickerPack, finalException);
            });
        });
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
                                  OperationCallback<StickerPack> callbackClass) {
        executor.execute(() -> {
            StickerException exception = null;
            try {
                callbackClass.onProgressUpdate(20);
                stickerPackRepository.remove(stickerPack);

                callbackClass.onProgressUpdate(60);
                resourceManagement.deleteFile(resourceManagement.getOrCreateStickerPackDirectory(stickerPack.getFolderName()));

                callbackClass.onProgressUpdate(90);
            } catch (StickerException ex) {
                exception = ex;
            }
            StickerException finalException = exception;
            threadResultPoster.post(() -> callbackClass.onCreationFinish(null, finalException));
        });
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

    public void createSticker(StickerPack stickerPack, Uri uriStickerImage, OperationCallback<Sticker> stickerCreationCallback) {
        executor.execute(() -> {
            Sticker sticker = null;
            StickerException exception = null;
            try {
                sticker = stickerService.createSticker(stickerPack, uriStickerImage, stickerCreationCallback);
                stickerPackRepository.update(stickerPack);
            } catch (StickerException ex) {
                exception = ex;
            }
            StickerException finalException = exception;
            Sticker finalSticker = sticker;
            threadResultPoster.post(() -> stickerCreationCallback.onCreationFinish(finalSticker, finalException));
        });
    }

    public void deleteSticker(Sticker sticker, StickerPack stickerPack) throws StickerException {
        stickerService.deleteSticker(sticker, stickerPack);
        stickerPackRepository.update(stickerPack);
    }
}
