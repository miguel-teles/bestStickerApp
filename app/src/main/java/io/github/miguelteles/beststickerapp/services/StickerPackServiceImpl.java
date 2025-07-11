package io.github.miguelteles.beststickerapp.services;

import android.content.ContentResolver;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.github.miguelteles.beststickerapp.R;
import io.github.miguelteles.beststickerapp.domain.entity.StickerPack;
import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.exception.StickerFolderException;
import io.github.miguelteles.beststickerapp.exception.enums.StickerExceptionEnum;
import io.github.miguelteles.beststickerapp.exception.enums.StickerFolderExceptionEnum;
import io.github.miguelteles.beststickerapp.repository.MyDatabase;
import io.github.miguelteles.beststickerapp.repository.StickerPackRepository;
import io.github.miguelteles.beststickerapp.repository.contentProvider.StickerUriProvider;
import io.github.miguelteles.beststickerapp.services.interfaces.FoldersManagementService;
import io.github.miguelteles.beststickerapp.services.interfaces.StickerPackService;
import io.github.miguelteles.beststickerapp.services.interfaces.StickerService;
import io.github.miguelteles.beststickerapp.utils.Utils;
import io.github.miguelteles.beststickerapp.validator.StickerPackValidator;
import io.github.miguelteles.beststickerapp.view.interfaces.UiThreadPoster;
import io.github.miguelteles.beststickerapp.view.threadHandlers.AndroidUiThreadPoster;
import io.github.miguelteles.beststickerapp.view.threadHandlers.ImmediateUiThreadPoster;

public class StickerPackServiceImpl implements StickerPackService {

    private static StickerPackService instance;
    private final FoldersManagementService foldersManagementService;
    private final StickerUriProvider stickerUriProvider;
    private final StickerPackRepository stickerPackRepository;
    private final ContentResolver contentResolver;
    private final StickerService stickerService;
    private final StickerPackValidator stickerPackValidator;
    private final Resources resources;
    private final Executor executor;
    private final UiThreadPoster threadResultPoster;


    private StickerPackServiceImpl() throws StickerException {
        this.stickerPackRepository = new StickerPackRepository(MyDatabase.getInstance().getSqLiteDatabase());
        this.foldersManagementService = FoldersManagementServiceImpl.getInstance();
        this.stickerUriProvider = StickerUriProvider.getInstance();
        this.contentResolver = Utils.getApplicationContext().getContentResolver();
        this.stickerPackValidator = StickerPackValidator.getInstance();
        this.stickerService = StickerServiceImpl.getInstance();
        this.resources = Utils.getApplicationContext().getResources();
        this.executor = Executors.newSingleThreadExecutor();
        this.threadResultPoster = new AndroidUiThreadPoster();
    }

    public StickerPackServiceImpl(FoldersManagementService foldersManagementService,
                                  StickerUriProvider stickerUriProvider,
                                  StickerPackRepository stickerPackRepository,
                                  ContentResolver contentResolver,
                                  StickerService stickerService,
                                  StickerPackValidator stickerPackValidator,
                                  Resources resources,
                                  Executor executor) {
        this.foldersManagementService = foldersManagementService;
        this.stickerUriProvider = stickerUriProvider;
        this.stickerPackRepository = stickerPackRepository;
        this.contentResolver = contentResolver;
        this.stickerService = stickerService;
        this.stickerPackValidator = stickerPackValidator;
        this.resources = resources;
        this.executor = executor;
        this.threadResultPoster = new ImmediateUiThreadPoster();
    }

    public static StickerPackService getInstance() throws StickerException {
        if (instance == null) {
            instance = new StickerPackServiceImpl();
        }
        return instance;
    }

    @Override
    public void createStickerPack(String authorNameInput,
                                  String packNameInput,
                                  Uri selectedImagemUri,
                                  StickerPackCreationCallback callback) {
        validateParametersCreateStickerPack(packNameInput, selectedImagemUri, callback);
        if (Utils.isNothing(authorNameInput)) {
            authorNameInput = resources.getString(R.string.defaultPublisher);
        }
        final String authorName = authorNameInput;

        executor.execute(() -> {
            StickerPack stickerPack = null;
            StickerException exception = null;

            File stickerPackFolder = null;
            try {
                callback.onProgressUpdate(10);
                String stickerPackFolderName = packNameInput + Utils.formatData(new Date(), "yyyy.MM.dd.HH.mm.ss");
                stickerPackFolder = foldersManagementService.getStickerPackFolderByFolderName(stickerPackFolderName);
                FoldersManagementServiceImpl.Image copiedImages = foldersManagementService.generateStickerImages(stickerPackFolder,
                        selectedImagemUri,
                        generateStickerPackImageName(),
                        FoldersManagementServiceImpl.TRAY_IMAGE_SIZE,
                        true);
                callback.onProgressUpdate(50);
                stickerPack = new StickerPack(null,
                        packNameInput,
                        authorName,
                        copiedImages.getOriginalImageFile().getName(),
                        copiedImages.getResizedImageFile().getName(),
                        stickerPackFolderName,
                        "1",
                        false,
                        copiedImages.getResidezImageFileInBytes());
                stickerPackValidator.verifyCreatedStickerPackValidity(stickerPack);
                stickerPackRepository.save(stickerPack);
                callback.onProgressUpdate(70);
                addStickerPackToContentProvider(stickerPack);
            } catch (StickerException ex) {
                deletePackFolderOnException(stickerPackFolder);
                exception = ex;
            }
            callback.onProgressUpdate(90);

            StickerException finalException = exception;
            StickerPack finalStickerPack = stickerPack;
            threadResultPoster.post(() -> {
                callback.onCreationFinish(finalStickerPack, finalException);
            });
        });

    }

    private void validateParametersCreateStickerPack(String packName, Uri packImageUri, StickerPackCreationCallback callback) {
        if (Utils.isNothing(packName)) {
            throw new IllegalArgumentException("Pack name cannot be null or empty");
        }
        if (packImageUri == null) {
            throw new IllegalArgumentException("Pack requires a image");
        }
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }
    }

    @NonNull
    private String generateStickerPackImageName() {
        return "packImg" + Utils.formatData(new Date(), "yyyyMMddHHmmss");
    }

    private void addStickerPackToContentProvider(StickerPack stickerPack) throws StickerException {
        if (stickerPack.getIdentifier() != null) {
            contentResolver.insert(stickerUriProvider.getStickerPackInsertUri(), stickerPack.toContentValues());
        } else {
            throw new StickerException(null, StickerExceptionEnum.CSP, "Erro ao salvar pacote no banco");
        }
    }

    private void deletePackFolderOnException(File stickerPackFolder) {
        try {
            foldersManagementService.deleteFile(stickerPackFolder);
        } catch (Exception e) {
        }
    }

    @Override
    public void updateStickerPack(StickerPack stickerPack,
                                  String editedAuthorName,
                                  String editedPackName,
                                  StickerPackCreationCallback callback) {
        if (Utils.isNothing(editedAuthorName)) {
            editedAuthorName = resources.getString(R.string.defaultPublisher);
        }
        final String _editedAuthorName = editedAuthorName;
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
                contentResolver.update(stickerUriProvider.getStickerPackUpdateUri(), stickerPack.toContentValues(), null, null);
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

    @Override
    public void deleteStickerPack(StickerPack stickerPack) throws StickerException {
        stickerPackRepository.remove(stickerPack);
        foldersManagementService.deleteStickerPackFolder(stickerPack.getFolderName());

        contentResolver.delete(stickerUriProvider.getStickerDeleteUri(), null, null);
    }

    @Override
    public StickerPack fetchStickerPackByIdWithAssets(StickerPack stickerPack) throws StickerException {
        StickerPack pack = stickerPackRepository.findById(stickerPack.getIdentifier());
        loadStickerPackAssets(pack);
        return pack;
    }

    @Override
    public List<StickerPack> fetchAllStickerPacksWithAssets() throws StickerException {
        List<StickerPack> packs = stickerPackRepository.findAll();
        for (StickerPack pack : packs) {
            loadStickerPackAssets(pack);
        }
        return packs;
    }

    private void loadStickerPackAssets(StickerPack pack) throws StickerException {
        pack.setStickers(stickerService.fetchAllStickerFromPackWithAssets(pack.getIdentifier()));
        final byte[] bytes;
        try {
            bytes = fetchStickerPackAsset(pack.getIdentifier(),
                    pack.getResizedTrayImageFile());
            if (bytes.length == 0) {
                throw new IllegalStateException("Asset file is empty, pack identifier: " + pack.getIdentifier());
            }
            pack.setResizedTrayImageFileInBytes(bytes);
        } catch (IllegalArgumentException e) {
            throw new StickerException(e, StickerExceptionEnum.FS, "Erro ao buscar figurinhas do pacote " + pack.getIdentifier());
        }
    }

    @Override
    public List<StickerPack> fetchAllStickerPacksWithoutAssets() throws StickerException {
        List<StickerPack> packs = stickerPackRepository.findAll();
        for (StickerPack pack : packs) {
            pack.setStickers(stickerService.fetchAllStickerFromPackWithoutAssets(pack.getIdentifier()));
        }
        return packs;
    }

    @Override
    public byte[] fetchStickerPackAsset(@NonNull Integer packIdentifier, @NonNull String stickerPackImageFileName) throws StickerFolderException {
        try (final InputStream inputStream = contentResolver.openInputStream(stickerUriProvider.getStickerPackResizedAssetUri(packIdentifier.toString(), stickerPackImageFileName));
             final ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            if (inputStream == null) {
                throw new IOException("cannot read sticker pack asset id: " + packIdentifier + "; name: " + stickerPackImageFileName);
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
