package com.example.samplestickerapp.modelView;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.example.samplestickerapp.exception.StickerException;
import com.example.samplestickerapp.exception.enums.StickerDBExceptionEnum;
import com.example.samplestickerapp.model.Sticker;
import com.example.samplestickerapp.model.StickerPack;
import com.example.samplestickerapp.repository.MyDatabase;
import com.example.samplestickerapp.repository.implementations.StickerRepository;
import com.example.samplestickerapp.utils.Folders;
import com.example.samplestickerapp.utils.Utils;
import com.example.samplestickerapp.view.StickerPackLoader;

import java.io.File;
import java.util.Date;

public class StickerViewModel extends ViewModel {

    private final StickerRepository stickerRepository;

    private final String STICKER_IMAGE_NAME = "sticker";

    public StickerViewModel(MyDatabase myDatabase) throws StickerException {
        this.stickerRepository = new StickerRepository(myDatabase.getMyDB());
    }

    public Sticker createSticker(StickerPack stickerPack,
                              Uri uriStickerImage,
                              Context context) throws StickerException {

        File stickerPackFolder = Folders.getStickerPackFolderByFolderName(stickerPack.getFolderName(), context);
        Folders.Image copiedImages = Folders.generateStickerImages(stickerPackFolder,
                Folders.getRealPathFromURI(uriStickerImage, context),
                generateStickerImageName(),
                Folders.STICKER_IMAGE_SIZE,
                false);

        Sticker sticker = new Sticker(copiedImages.getResizedImageFileName(), stickerPack.getIdentifier());
        stickerRepository.save(sticker, context);
        if (sticker.getIdentifier() != null) {
            context.getContentResolver().insert(StickerPackLoader.getStickerInsertUri(),sticker.toContentValues());
            return sticker;
        } else {
            throw new StickerException(null, StickerDBExceptionEnum.INSERT, "Erro ao salvar pacote no banco");
        }

    }

    private String generateStickerImageName() {
        return STICKER_IMAGE_NAME + Utils.formatData(new Date(), "yyyyMMddHHmmss");
    }
}
