package io.github.miguelteles.beststickerapp.repository;

public abstract class CommonRepository {

    protected String FIND_BY_ID = "SELECT * FROM %s WHERE identifier=?";
    protected String DELETE_BY_ID = "DELETE FROM %s WHERE identifier=?";
    protected String FIND_ALL = "SELECT * FROM %s";

    public CommonRepository(String nmTabela) {
        DELETE_BY_ID = String.format(DELETE_BY_ID, nmTabela);
        FIND_BY_ID = String.format(FIND_BY_ID, nmTabela);
        FIND_ALL = String.format(FIND_ALL, nmTabela);
    }
}
