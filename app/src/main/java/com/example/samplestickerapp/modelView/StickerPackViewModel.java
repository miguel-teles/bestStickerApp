package com.example.samplestickerapp.modelView;

import android.content.Context;
import android.net.Uri;

import androidx.lifecycle.ViewModel;

import com.example.samplestickerapp.R;
import com.example.samplestickerapp.exception.StickerException;
import com.example.samplestickerapp.exception.enums.StickerDBExceptionEnum;
import com.example.samplestickerapp.exception.enums.StickerExceptionEnum;
import com.example.samplestickerapp.model.StickerPack;
import com.example.samplestickerapp.repository.MyDatabase;
import com.example.samplestickerapp.repository.implementations.StickerPackRepository;
import com.example.samplestickerapp.repository.implementations.StickerRepository;
import com.example.samplestickerapp.utils.Folders;
import com.example.samplestickerapp.utils.Utils;

import java.io.File;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class StickerPackViewModel extends ViewModel {

    private final StickerPackRepository stickerPackRepository;
    private final StickerRepository stickerRepository;

    private final String STICKER_PACK_IMAGE_NAME = "packImg";

    public StickerPackViewModel(MyDatabase myDatabase) throws StickerException {
        this.stickerPackRepository = new StickerPackRepository(myDatabase.getMyDB());
        this.stickerRepository = new StickerRepository(myDatabase.getMyDB());
    }

    public StickerPack createStickerPack(String nmAutorInput,
                                         String nmPacoteInput,
                                         Uri uriImagemStickerPackInput,
                                         boolean isAnimated,
                                         Context context) throws StickerException {
        if (Utils.isNothing(nmAutorInput)) {
            nmAutorInput = context.getResources().getString(R.string.defaultPublisher);
        }
        String stickerPackFolderName = nmPacoteInput + Utils.formatData(new Date(), "yyyy.MM.dd.HH.mm.ss");
        File stickerPackFolder = null;
        try {
            stickerPackFolder = Folders.makeDirPackIdentifier(stickerPackFolderName, context);
            File[] imgsCopiada = Folders.copiaFotoParaPastaPacote(stickerPackFolderName,
                    Folders.getRealPathFromURI(uriImagemStickerPackInput, context),
                    STICKER_PACK_IMAGE_NAME + Utils.formatData(new Date(), "yyyyMMddHHmmss"),
                    Folders.TRAY_IMAGE_SIZE,
                    Folders.TRAY_IMAGE_MAX_FILE_SIZE,
                    context);
            StickerPack stickerPack = new StickerPack(null,
                    nmPacoteInput,
                    nmAutorInput,
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
                Folders.deleteFile(stickerPackFolder);
            } catch (Exception e) {
            }
            throw ex;
        }
    }

    public List<StickerPack> fetchStickerPacks() throws StickerException {
        return stickerPackRepository.findAll();
    }

    public StickerPack updateStickerPack(StickerPack stickerPack,
                                         String nmAutorInput,
                                         String nmPacoteInput,
                                         Context applicationContext) throws StickerException {
        stickerPack.setName(nmPacoteInput);
        stickerPack.setPublisher(nmAutorInput);
        StickerPack updatedStickerPack = stickerPackRepository.update(stickerPack, applicationContext);
        return updatedStickerPack;
    }

    public void deleteStickerPack(StickerPack stickerPack, Context applicationContext) throws StickerException {
        stickerPackRepository.remove(stickerPack, applicationContext);

        //TODO: FALTA APAGAR A PASTA E AS IMAGENS DENTRO!

        //todo:  testar isso
        Folders.deleteStickerPackFolder(stickerPack.getFolder(), applicationContext);

    }
}
