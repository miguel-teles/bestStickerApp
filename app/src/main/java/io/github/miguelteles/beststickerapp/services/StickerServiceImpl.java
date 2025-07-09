package io.github.miguelteles.beststickerapp.services;

import android.content.Context;
import android.net.Uri;

import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.exception.enums.StickerExceptionEnum;
import io.github.miguelteles.beststickerapp.domain.entity.Sticker;
import io.github.miguelteles.beststickerapp.domain.entity.StickerPack;
import io.github.miguelteles.beststickerapp.repository.MyDatabase;
import io.github.miguelteles.beststickerapp.repository.StickerCommonRepository;
import io.github.miguelteles.beststickerapp.foldersManagement.Folders;
import io.github.miguelteles.beststickerapp.repository.contentProvider.StickerUriProvider;
import io.github.miguelteles.beststickerapp.services.interfaces.StickerService;
import io.github.miguelteles.beststickerapp.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class StickerServiceImpl implements StickerService {

    private static StickerService instance;
    private final StickerCommonRepository stickerRepository;

    private StickerServiceImpl(Context context) throws StickerException {
        this.stickerRepository = new StickerCommonRepository(MyDatabase.getInstance(context).getSqLiteDatabase());
    }

    public static StickerService getInstance(Context context) throws StickerException {
        if (instance == null) {
            instance = new StickerServiceImpl(context);
        }
        return instance;
    }

    @Override
    public Sticker createSticker(StickerPack stickerPack,
                                 Uri uriStickerImage,
                                 Context context) throws StickerException {

        File stickerPackFolder = Folders.getStickerPackFolderByFolderName(stickerPack.getFolderName(), context);
        Folders.Image copiedImages = Folders.generateStickerImages(stickerPackFolder,
                Folders.getAbsolutePathFromURI(uriStickerImage, context),
                generateStickerImageName(),
                Folders.STICKER_IMAGE_SIZE,
                false);

        Sticker sticker = new Sticker(copiedImages.getResizedImageFile().getName(), stickerPack.getIdentifier(), copiedImages.getResizedImageFile().length());
        stickerRepository.save(sticker, context);
        if (sticker.getIdentifier() != null) {
            context.getContentResolver().insert(StickerUriProvider.getStickerInsertUri(), sticker.toContentValues());
            return sticker;
        } else {
            throw new StickerException(null, StickerExceptionEnum.CSP, "Erro ao salvar pacote no banco");
        }
    }

    @Override
    public void deleteSticker(Sticker sticker,
                              StickerPack stickerPack,
                              Context context) throws StickerException {
        stickerRepository.remove(sticker, context);

        File stickerPackFolder = Folders.getStickerPackFolderByFolderName(stickerPack.getFolderName(), context);
        Folders.deleteFile(new File(stickerPackFolder, sticker.getStickerImageFile()));

        context.getContentResolver().delete(StickerUriProvider.getStickerDeleteUri(), null);
    }

    @Override
    public List<Sticker> fetchAllStickerFromPack(StickerPack stickerPack, Context context) throws StickerException {
        List<Sticker> stickers = stickerRepository.findByPackIdentifier(stickerPack.getIdentifier());

        for (Sticker sticker : stickers) {
            final byte[] bytes;
            try {
                bytes = StickerService.fetchStickerAsset(stickerPack.getIdentifier().toString(),
                        sticker.getStickerImageFile(),
                        context.getContentResolver());
                if (bytes.length <= 0) {
                    throw new IllegalStateException("Asset file is empty, pack: " + stickerPack.getName() + ", sticker: " + sticker.getStickerImageFile());
                }
                sticker.setSize(bytes.length);
            } catch (IOException | IllegalArgumentException e) {
                throw new IllegalStateException("Asset file doesn't exist. pack: " + stickerPack.getName() + ", sticker: " + sticker.getStickerImageFile(), e);
            }
        }

        return stickers;
    }

    private String generateStickerImageName() {
        return "sticker" + Utils.formatData(new Date(), "yyyyMMddHHmmss");
    }
}
