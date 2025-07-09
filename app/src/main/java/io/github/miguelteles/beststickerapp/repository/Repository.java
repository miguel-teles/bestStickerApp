package io.github.miguelteles.beststickerapp.repository;

import io.github.miguelteles.beststickerapp.repository.interfaces.RepositoryInterface;

public abstract class Repository<T> implements RepositoryInterface<T> {

    protected String FIND_BY_ID = "SELECT * FROM %s WHERE identifier=?";
    protected String DELETE_BY_ID = "DELETE FROM %s WHERE identifier=?";
    protected String FIND_ALL = "SELECT * FROM %s";

    public Repository(String nmTabela) {
        DELETE_BY_ID = String.format(DELETE_BY_ID, nmTabela);
        FIND_BY_ID = String.format(FIND_BY_ID, nmTabela);
        FIND_ALL = String.format(FIND_ALL, nmTabela);
    }
}
