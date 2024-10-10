package com.example.samplestickerapp.modelView;

import android.content.Context;
import android.net.Uri;

import androidx.lifecycle.ViewModel;

import com.example.samplestickerapp.R;
import com.example.samplestickerapp.exception.StickerException;
import com.example.samplestickerapp.exception.StickerExceptionHandler;
import com.example.samplestickerapp.exception.enums.StickerDBExceptionEnum;
import com.example.samplestickerapp.model.StickerPack;
import com.example.samplestickerapp.repository.MyDatabase;
import com.example.samplestickerapp.repository.implementations.StickerPackRepository;
import com.example.samplestickerapp.repository.implementations.StickerRepository;
import com.example.samplestickerapp.utils.Folders;
import com.example.samplestickerapp.utils.Utils;

import java.io.File;
import java.util.Date;

public class StickerPackViewModel extends ViewModel {

    private final StickerPackRepository stickerPackRepository;
    private final StickerRepository stickerRepository;

    private final String STICKER_PACK_IMAGE_NAME = "packImg";

    public StickerPackViewModel(MyDatabase myDatabase) throws StickerException {
        this.stickerPackRepository = new StickerPackRepository(myDatabase.getMyDB());
        this.stickerRepository = new StickerRepository(myDatabase.getMyDB());
    }

    public StickerPack createStickerPack(String publisher,
                                          String nomePacote,
                                          Uri uriImagemStickerPack,
                                          boolean isAnimated,
                                          Context context) {
        if (Utils.isNothing(publisher)) {
            publisher = context.getResources().getString(R.string.defaultPublisher);
        }
        String stickerPackFolderName = nomePacote + Utils.formatData(new Date(), "yyyy.MM.dd.HH.mm.ss");
        File stickerPackFolder = null;
        try {
            stickerPackFolder = Folders.makeDirPackIdentifier(stickerPackFolderName, context);
            File[] imgsCopiada = Folders.copiaFotoParaPastaPacote(stickerPackFolderName,
                    Folders.getRealPathFromURI(uriImagemStickerPack, context),
                    STICKER_PACK_IMAGE_NAME,
                    Folders.TRAY_IMAGE_SIZE,
                    Folders.TRAY_IMAGE_MAX_FILE_SIZE,
                    context);
            StickerPack stickerPack = new StickerPack(null,
                    nomePacote,
                    publisher,
                    imgsCopiada[0].getPath(),
                    imgsCopiada[1].getPath(),
                    stickerPackFolderName,
                    "1",
                    isAnimated);
            stickerPackRepository.save(stickerPack, context);
            if (stickerPack.getIdentifier() != null) {
                return stickerPack;
            } else {
                throw new StickerException(null, StickerDBExceptionEnum.INSERT, "Erro ao salvar pacote no banco");
            }
        } catch (StickerException ex) {
            try {
                Folders.deleteStickerPackFolder(stickerPackFolder, context);
            } catch (Exception e) {
            }
            StickerExceptionHandler.handleException(ex, context);
        }
        return null;
    }
}
