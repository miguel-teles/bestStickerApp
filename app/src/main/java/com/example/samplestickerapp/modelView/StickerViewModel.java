package com.example.samplestickerapp.modelView;

import android.content.Context;
import android.net.Uri;

import androidx.lifecycle.ViewModel;

import com.example.samplestickerapp.exception.StickerException;
import com.example.samplestickerapp.exception.enums.StickerDBExceptionEnum;
import com.example.samplestickerapp.model.Sticker;
import com.example.samplestickerapp.model.StickerPack;
import com.example.samplestickerapp.repository.MyDatabase;
import com.example.samplestickerapp.repository.implementations.StickerPackRepository;
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

        File[] imgsCopiada = Folders.copiaFotoParaPastaPacote(stickerPack.getFolder(),
                Folders.getRealPathFromURI(uriStickerImage, context),
                STICKER_IMAGE_NAME + Utils.formatData(new Date(), "yyyyMMddHHmmss"),
                Folders.STICKER_IMAGE_SIZE,
                Folders.STICKER_IMAGE_MAX_FILE_SIZE,
                false,
                context);

        Sticker sticker = new Sticker(imgsCopiada[1].getPath(), stickerPack.getIdentifier());
        stickerRepository.save(sticker, context);
        if (sticker.getIdentifier() != null) {
            context.getContentResolver().insert(StickerPackLoader.getStickerInsertUri(),sticker.toContentValues());
            return sticker;
        } else {
            throw new StickerException(null, StickerDBExceptionEnum.INSERT, "Erro ao salvar pacote no banco");
        }

    }
}
