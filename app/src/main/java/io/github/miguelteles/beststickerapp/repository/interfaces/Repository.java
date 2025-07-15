package io.github.miguelteles.beststickerapp.repository.interfaces;

import android.content.Context;

import io.github.miguelteles.beststickerapp.domain.entity.StickerPack;
import io.github.miguelteles.beststickerapp.exception.StickerException;

import java.util.List;
import java.util.UUID;

public interface Repository<T> {

    T save(T obj) throws StickerException;
    T update(T obj) throws StickerException;
    Integer remove(T obj) throws StickerException;
    Integer remove(UUID id) throws StickerException;
    T findById(UUID id) throws StickerException;
    List<T> findAll() throws StickerException;
}
