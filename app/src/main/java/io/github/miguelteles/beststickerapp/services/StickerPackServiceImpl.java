package io.github.miguelteles.beststickerapp.services;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import io.github.miguelteles.beststickerapp.R;
import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.exception.enums.StickerExceptionEnum;
import io.github.miguelteles.beststickerapp.domain.entity.StickerPack;
import io.github.miguelteles.beststickerapp.repository.MyDatabase;
import io.github.miguelteles.beststickerapp.repository.StickerPackRepository;
import io.github.miguelteles.beststickerapp.foldersManagement.Folders;
import io.github.miguelteles.beststickerapp.repository.contentProvider.StickerUriProvider;
import io.github.miguelteles.beststickerapp.services.interfaces.StickerPackService;
import io.github.miguelteles.beststickerapp.utils.Utils;
import io.github.miguelteles.beststickerapp.repository.contentProvider.StickerContentProviderReader;

import java.io.File;
import java.util.Date;
import java.util.List;

public class StickerPackServiceImpl implements StickerPackService {

    private static StickerPackServiceImpl instance;
    private final StickerPackRepository stickerPackRepository;
    private final String STICKER_PACK_IMAGE_NAME = "packImg";

    private StickerPackServiceImpl(Context context) {
        this.stickerPackRepository = new StickerPackRepository(MyDatabase.getInstance(context).getSqLiteDatabase());
    }

    public static StickerPackServiceImpl getInstace(Context context) {
        if (instance == null) {
            instance = new StickerPackServiceImpl(context);
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
            stickerPackFolder = Folders.getStickerPackFolderByFolderName(stickerPackFolderName, context);
            Folders.Image copiedImages = Folders.generateStickerImages(stickerPackFolder,
                    Folders.getRealPathFromURI(uriImagemStickerPackInput, context),
                    generateStickerPackImageName(),
                    Folders.TRAY_IMAGE_SIZE,
                    true);
            StickerPack stickerPack = new StickerPack(null,
                    nomePacote,
                    nomeAutor,
                    copiedImages.getOriginalImageFile().getName(),
                    copiedImages.getResizedImageFile().getName(),
                    stickerPackFolderName,
                    "1",
                    false);
            stickerPackRepository.save(stickerPack, context);
            addStickerPackToContentProvider(context, stickerPack);
            return stickerPack;
        } catch (StickerException ex) {
            deletePackFolderOnException(stickerPackFolder);
            throw ex;
        }
    }

    @NonNull
    private String generateStickerPackImageName() {
        return STICKER_PACK_IMAGE_NAME + Utils.formatData(new Date(), "yyyyMMddHHmmss");
    }

    private static void addStickerPackToContentProvider(Context context, StickerPack stickerPack) throws StickerException {
        if (stickerPack.getIdentifier() != null) {
            context.getContentResolver().insert(StickerUriProvider.getStickerPackInsertUri(), stickerPack.toContentValues());
        } else {
            throw new StickerException(null, StickerExceptionEnum.CSP, "Erro ao salvar pacote no banco");
        }
    }

    private static void deletePackFolderOnException(File stickerPackFolder) {
        try {
            Folders.deleteFile(stickerPackFolder);
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
        StickerPack updatedStickerPack = stickerPackRepository.update(stickerPack, context);
        context.getContentResolver().update(StickerUriProvider.getStickerPackUpdateUri(),stickerPack.toContentValues(),null,null);
        return updatedStickerPack;
    }

    @Override
    public void deleteStickerPack(StickerPack stickerPack,
                                  Context applicationContext) throws StickerException {
        stickerPackRepository.remove(stickerPack, applicationContext);
        Folders.deleteStickerPackFolder(stickerPack.getFolderName(), applicationContext);

        applicationContext.getContentResolver().delete(StickerUriProvider.getStickerDeleteUri(), null, null);
    }

    @Override
    public StickerPack fetchUpdatedStickerPack(StickerPack stickerPack) throws StickerException {
        return stickerPackRepository.findById(stickerPack.getIdentifier());
    }

    @Override
    public List<StickerPack> fetchAllStickerPacks() {
        return stickerPackRepository.findAll();
    }
}
