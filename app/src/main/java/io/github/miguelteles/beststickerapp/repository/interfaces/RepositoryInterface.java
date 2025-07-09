package io.github.miguelteles.beststickerapp.repository.interfaces;

import android.content.Context;

import io.github.miguelteles.beststickerapp.exception.StickerException;

import java.util.List;

public interface RepositoryInterface<T> {

    T save(T obj, Context context) throws StickerException;
    T update(T obj, Context context) throws StickerException;
    Integer remove(T obj, Context context) throws StickerException;
    Integer remove(Integer id, Context context) throws StickerException;
    T findById(Integer id) throws StickerException;
    List<T> findAll() throws StickerException;


}
