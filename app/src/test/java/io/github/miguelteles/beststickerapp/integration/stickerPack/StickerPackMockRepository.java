package io.github.miguelteles.beststickerapp.integration.stickerPack;

import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.github.miguelteles.beststickerapp.domain.entity.StickerPack;
import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.repository.StickerPackRepository;

public class StickerPackMockRepository extends StickerPackRepository {

    Map<UUID, StickerPack> stickerPackMap = new HashMap<>();

    public StickerPackMockRepository() {
        super(null);
    }

    @Override
    public StickerPack save(StickerPack stickerPack) throws StickerException {
        stickerPack.setIdentifier(UUID.randomUUID());

        stickerPackMap.put(stickerPack.getIdentifier(), stickerPack);
        return stickerPack;
    }

    @Override
    public StickerPack update(StickerPack stickerPack) throws StickerException {
        stickerPackMap.put(stickerPack.getIdentifier(), stickerPack);
        return stickerPack;
    }

    @Override
    public void remove(StickerPack stickerPack) throws StickerException {
        stickerPackMap.remove(stickerPack.getIdentifier());
    }

    @Override
    public void remove(UUID identifier) throws StickerException {
        stickerPackMap.remove(identifier);
    }

    @Override
    public StickerPack findById(UUID id) throws StickerException {
        return stickerPackMap.get(id);
    }

    @Override
    public List<StickerPack> findAll() throws StickerException {
        return new ArrayList<>(stickerPackMap.values());
    }
}
