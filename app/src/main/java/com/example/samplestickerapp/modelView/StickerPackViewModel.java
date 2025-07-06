package com.example.samplestickerapp.modelView;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.example.samplestickerapp.R;
import com.example.samplestickerapp.exception.StickerException;
import com.example.samplestickerapp.exception.enums.StickerDBExceptionEnum;
import com.example.samplestickerapp.model.StickerPack;
import com.example.samplestickerapp.repository.MyDatabase;
import com.example.samplestickerapp.repository.implementations.StickerPackRepository;
import com.example.samplestickerapp.utils.Folders;
import com.example.samplestickerapp.utils.Utils;
import com.example.samplestickerapp.view.StickerPackLoader;

import java.io.File;
import java.util.Date;
import java.util.List;

public class StickerPackViewModel extends ViewModel {

    private final StickerPackRepository stickerPackRepository;

    private final String STICKER_PACK_IMAGE_NAME = "packImg";

    public StickerPackViewModel(MyDatabase myDatabase) throws StickerException {
        this.stickerPackRepository = new StickerPackRepository(myDatabase.getMyDB());
    }

    public StickerPack createStickerPack(String nmAutorInput,
                                         String nmPacoteInput,
                                         Uri uriImagemStickerPackInput,
                                         Context context) throws StickerException {
        if (Utils.isNothing(nmAutorInput)) {
            nmAutorInput = context.getResources().getString(R.string.defaultPublisher);
        }
        File stickerPackFolder = null;
        try {
            String stickerPackFolderName = nmPacoteInput + Utils.formatData(new Date(), "yyyy.MM.dd.HH.mm.ss");
            stickerPackFolder = Folders.getStickerPackFolderByFolderName(stickerPackFolderName, context);
            Folders.Image copiedImages = Folders.generateStickerImages(stickerPackFolder,
                    Folders.getRealPathFromURI(uriImagemStickerPackInput, context),
                    generateStickerPackImageName(),
                    Folders.TRAY_IMAGE_SIZE,
                    true);
            StickerPack stickerPack = new StickerPack(null,
                    nmPacoteInput,
                    nmAutorInput,
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
            context.getContentResolver().insert(StickerPackLoader.getStickerPackInsertUri(), stickerPack.toContentValues());
        } else {
            throw new StickerException(null, StickerDBExceptionEnum.INSERT, "Erro ao salvar pacote no banco");
        }
    }

    private static void deletePackFolderOnException(File stickerPackFolder) {
        try {
            Folders.deleteFile(stickerPackFolder);
        } catch (Exception e) {
        }
    }

    public StickerPack updateStickerPack(StickerPack stickerPack,
                                         String nmAutorInput,
                                         String nmPacoteInput,
                                         Context applicationContext) throws StickerException {
        stickerPack.setName(nmPacoteInput);
        stickerPack.setPublisher(nmAutorInput);
        StickerPack updatedStickerPack = stickerPackRepository.update(stickerPack, applicationContext);
        applicationContext.getContentResolver().update(StickerPackLoader.getStickerPackUpdateUri(),stickerPack.toContentValues(),null,null);
        return updatedStickerPack;
    }

    public void deleteStickerPack(StickerPack stickerPack, Context applicationContext) throws StickerException {
        stickerPackRepository.remove(stickerPack, applicationContext);
        Folders.deleteStickerPackFolder(stickerPack.getFolderName(), applicationContext);
    }

    public StickerPack fetchUpdatedStickerPack(StickerPack stickerPack) throws StickerException {
        return stickerPackRepository.findById(stickerPack.getIdentifier());
    }
}
