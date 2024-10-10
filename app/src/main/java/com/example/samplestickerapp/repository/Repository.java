package com.example.samplestickerapp.repository;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.example.samplestickerapp.exception.StickerException;
import com.example.samplestickerapp.repository.interfaces.RepositoryInterface;

public abstract class Repository<T> implements RepositoryInterface<T> {

    protected String FIND_BY_ID;
    protected String DELETAR_BY_ID;
    protected String FIND_ALL;

    public Repository(String nmTabela) {
        DELETAR_BY_ID = String.format(RepositoryInterface.DELETE_BY_ID, nmTabela);
        FIND_BY_ID = String.format(RepositoryInterface.FIND_BY_ID, nmTabela);
        FIND_ALL = String.format(RepositoryInterface.FIND_ALL, nmTabela);
    }
}
