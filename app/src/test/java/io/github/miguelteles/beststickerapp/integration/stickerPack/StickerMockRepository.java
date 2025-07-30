package io.github.miguelteles.beststickerapp.integration.stickerPack;

import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.github.miguelteles.beststickerapp.domain.entity.Sticker;
import io.github.miguelteles.beststickerapp.exception.StickerDataBaseException;
import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.repository.StickerRepository;

public class StickerMockRepository extends StickerRepository {

    Map<UUID, Sticker> stickerMap = new HashMap<>();
    public StickerMockRepository() {
        super(null);
    }

    @Override
    public Sticker save(Sticker sticker) throws StickerException {
        sticker.setIdentifier(UUID.randomUUID());
        stickerMap.put(sticker.getIdentifier(), sticker);
        return sticker;
    }

    @Override
    public Sticker update(Sticker obj) throws StickerException {
        return null;
    }

    @Override
    public void remove(Sticker sticker) throws StickerException {
        stickerMap.remove(sticker.getIdentifier());
    }

    @Override
    public void remove(UUID identifier) throws StickerException {
        stickerMap.remove(identifier);
    }

    @Override
    public void removeByPackIdentifier(UUID packIdentifier) throws StickerException {
        for (Sticker sticker : findByPackIdentifier(packIdentifier)) {
            stickerMap.remove(sticker.getIdentifier());
        }
    }

    @Override
    public Sticker findById(UUID id) throws StickerException {
        return stickerMap.get(id);
    }

    @Override
    public List<Sticker> findAll() throws StickerException {
        return new ArrayList<>(stickerMap.values());
    }

    @Override
    public List<Sticker> findByPackIdentifier(UUID packIdentifier) throws StickerDataBaseException {
        List<Sticker> stickersToRemove = new ArrayList<>();
        for (Map.Entry<UUID, Sticker> stickerEntry : stickerMap.entrySet()) {
            if (stickerEntry.getValue().getPackIdentifier().equals(packIdentifier)) {
                stickersToRemove.add(stickerEntry.getValue());
            }
        }
        return stickersToRemove;
    }
}
