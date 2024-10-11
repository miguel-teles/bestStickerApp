package com.example.samplestickerapp.repository.interfaces;

import android.content.Context;

import com.example.samplestickerapp.exception.StickerException;

import java.util.List;

public interface RepositoryInterface<T> {
    String DELETE_BY_ID = "DELETE FROM %s WHERE identifier=?";
    String FIND_BY_ID = "SELECT * FROM %s WHERE identifier=?";
    String FIND_ALL = "SELECT * FROM %s";

    T save(T obj, Context context) throws StickerException;
    T update(T obj, Context context) throws StickerException;
    Integer remove(T obj, Context context) throws StickerException;
    Integer remove(Integer id, Context context) throws StickerException;
    T find(Integer id) throws StickerException;
    List<T> findAll() throws StickerException;


}
