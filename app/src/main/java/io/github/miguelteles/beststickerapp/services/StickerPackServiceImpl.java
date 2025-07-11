package io.github.miguelteles.beststickerapp.services;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

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

public class StickerPackServiceImpl implements StickerPackService {

    private static StickerPackService instance;
    private final FoldersManagementService foldersManagementService;
    private final StickerUriProvider stickerUriProvider;
    private final StickerPackRepository stickerPackRepository;
    private final ContentResolver contentResolver;

    private final StickerService stickerService;
    private final StickerPackValidator stickerPackValidator;

    private StickerPackServiceImpl() throws StickerException {
        this.stickerPackRepository = new StickerPackRepository(MyDatabase.getInstance().getSqLiteDatabase());
        foldersManagementService = FoldersManagementServiceImpl.getInstance();
        stickerUriProvider = StickerUriProvider.getInstance();
        contentResolver = Utils.getApplicationContext().getContentResolver();
        stickerPackValidator = StickerPackValidator.getInstance();
        stickerService = StickerServiceImpl.getInstance();
    }


    public static StickerPackService getInstance() throws StickerException {
        if (instance == null) {
            instance = new StickerPackServiceImpl();
        }
        return instance;
    }

    @Override
    public StickerPack createStickerPack(String nomeAutor,
                                         String nomePacote,
                                         Uri uriImagemStickerPackInput,
                                         Context context) throws StickerException {
        if (Utils.isNothing(nomeAutor)) {
            nomeAutor = context.getResources().getString(R.string.defaultPublisher);
        }
        File stickerPackFolder = null;
        try {
            String stickerPackFolderName = nomePacote + Utils.formatData(new Date(), "yyyy.MM.dd.HH.mm.ss");
            stickerPackFolder = foldersManagementService.getStickerPackFolderByFolderName(stickerPackFolderName);
            FoldersManagementServiceImpl.Image copiedImages = foldersManagementService.generateStickerImages(stickerPackFolder,
                    uriImagemStickerPackInput,
                    generateStickerPackImageName(),
                    FoldersManagementServiceImpl.TRAY_IMAGE_SIZE,
                    true);
            StickerPack stickerPack = new StickerPack(null,
                    nomePacote,
                    nomeAutor,
                    copiedImages.getOriginalImageFile().getName(),
                    copiedImages.getResizedImageFile().getName(),
                    stickerPackFolderName,
                    "1",
                    false);
            stickerPackValidator.verifyStickerPackValidity(stickerPack);
            stickerPackRepository.save(stickerPack);
            context.getContentResolver().insert(StickerUriProvider.getInstance().getStickerPackInsertUri(), stickerPack.toContentValues());
            addStickerPackToContentProvider(context, stickerPack);
            return stickerPack;
        } catch (StickerException ex) {
            deletePackFolderOnException(stickerPackFolder);
            throw ex;
        }
    }

    @NonNull
    private String generateStickerPackImageName() {
        return "packImg" + Utils.formatData(new Date(), "yyyyMMddHHmmss");
    }

    private void addStickerPackToContentProvider(Context context, StickerPack stickerPack) throws StickerException {
        if (stickerPack.getIdentifier() != null) {
            context.getContentResolver().insert(stickerUriProvider.getStickerPackInsertUri(), stickerPack.toContentValues());
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
    public StickerPack updateStickerPack(StickerPack stickerPack,
                                         String nomeAutor,
                                         String nomePacote,
                                         Context context) throws StickerException {
        stickerPack.setName(nomePacote);
        stickerPack.setPublisher(nomeAutor);
        StickerPack updatedStickerPack = stickerPackRepository.update(stickerPack);
        context.getContentResolver().update(stickerUriProvider.getStickerPackUpdateUri(), stickerPack.toContentValues(), null, null);
        return updatedStickerPack;
    }

    @Override
    public void deleteStickerPack(StickerPack stickerPack,
                                  Context applicationContext) throws StickerException {
        stickerPackRepository.remove(stickerPack);
        foldersManagementService.deleteStickerPackFolder(stickerPack.getFolderName());

        applicationContext.getContentResolver().delete(stickerUriProvider.getStickerDeleteUri(), null, null);
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
