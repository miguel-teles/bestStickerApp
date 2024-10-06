package com.example.samplestickerapp.repository;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.example.samplestickerapp.exception.StickerException;

public abstract class Repository<T> implements RepositoryInterface<T>{

    protected String SALVAR;
    protected String CONSULTAR_BY_ID;
    protected String DELETAR_BY_ID;

    public Repository(String nmTabela) {
        CONSULTAR_BY_ID = String.format(RepositoryInterface.FIND_BY_ID, nmTabela);
        DELETAR_BY_ID = String.format(RepositoryInterface.DELETE_BY_ID, nmTabela);
    }

    SQLiteDatabase getMyDB(Context context) throws StickerException {
        return MyDatabase.getMyDB(context);
    }
}
